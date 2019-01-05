package org.sysu.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.utility.CommonUtil;
import org.sysu.workflow.*;
import org.sysu.workflow.core.*;
import org.sysu.workflow.dao.RenArchivedTreeEntityDAO;
import org.sysu.workflow.dao.RenBinstepEntityDAO;
import org.sysu.workflow.dao.RenRuntimerecordEntityDAO;
import org.sysu.renCommon.entity.RenArchivedTreeEntity;
import org.sysu.renCommon.entity.RenBinstepEntity;
import org.sysu.workflow.core.env.MultiStateMachineDispatcher;
import org.sysu.workflow.core.env.SimpleErrorReporter;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;
import org.sysu.workflow.core.instanceTree.InstanceManager;
import org.sysu.workflow.core.instanceTree.RInstanceTree;
import org.sysu.workflow.core.instanceTree.RTreeNode;
import org.sysu.workflow.utility.LogUtil;
import org.sysu.workflow.utility.SerializationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Author: Rinkako
 * Date  : 2018/5/15
 * Usage : Methods for making business object service.
 */

@Service
public class SteadyStepService {

    @Autowired
    private RuntimeManagementService runtimeManagementService;

    @Autowired
    private RenBinstepEntityDAO renBinstepEntityDAO;

    @Autowired
    private RenArchivedTreeEntityDAO renArchivedTreeEntityDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    private boolean EnableSteadyStep = true;

    /**
     * Write a entity step to entity memory.
     *
     * @param exctx BOXML execution context
     */
    @Transactional(rollbackFor = Exception.class)
    public void WriteSteady(BOXMLExecutionContext exctx) {
        if (!this.EnableSteadyStep) {
            return;
        }
        try {
            RenBinstepEntity binStep = renBinstepEntityDAO.findByNodeId(exctx.NodeId);
            if (binStep == null) {
                binStep = new RenBinstepEntity();
                binStep.setRtid(exctx.Rtid);
                binStep.setNodeId(exctx.NodeId);
                binStep.setNotifiableId(exctx.NotifiableId);
                RInstanceTree tree = InstanceManager.GetInstanceTree(exctx.Rtid);
                RTreeNode parentNode = tree.GetNodeById(exctx.NodeId).Parent;
                binStep.setSupervisorId(parentNode != null ? parentNode.getExect().NodeId : "");
            }
            BOInstance boInstance = exctx.getSCXMLExecutor().detachInstance();
            binStep.setBinlog(SerializationUtil.SerializationBOInstanceToByteArray(boInstance));
            exctx.getSCXMLExecutor().attachInstance(boInstance);
            renBinstepEntityDAO.saveOrUpdate(binStep);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Write service entity step to DB failed, save action rollback.",
                    SteadyStepService.class.getName(), LogLevelType.ERROR, exctx.Rtid);
        }
    }

    /**
     * Clear entity step snapshot after final state, and write a span tree descriptor to archived tree table.
     *
     * @param rtid process runtime record id
     */
    @Transactional(rollbackFor = Exception.class)
    public void ClearSteadyWriteArchivedTree(String rtid) {
        try {
            renBinstepEntityDAO.deleteRenBinstepEntitiesByRtid(rtid);
            RenArchivedTreeEntity archivedTree = new RenArchivedTreeEntity();
            archivedTree.setRtid(rtid);
            archivedTree.setTree(SerializationUtil.JsonSerialization(runtimeManagementService.GetSpanTreeDescriptor(rtid), rtid));
            renArchivedTreeEntityDAO.saveOrUpdate(archivedTree);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Clear service entity step failed, action rollback.",
                    SteadyStepService.class.getName(), LogLevelType.ERROR, rtid);
        }
    }

    /**
     * Resume instances from entity memory, and register it to instance manager.
     *
     * @param rtidList rtid in JSON list
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<String> ResumeSteadyMany(String rtidList) {
        List<String> rtidItems = SerializationUtil.JsonDeserialization(rtidList, List.class);
        List<String> failedList = new ArrayList<>();
        for (String rtid : rtidItems) {
            if (!this.ResumeSteady(rtid)) {
                failedList.add(rtid);
            }
        }
        return failedList;
    }

    /**
     * Resume a instance from entity memory, and register it to instance manager.
     *
     * @param rtid process runtime record id
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean ResumeSteady(String rtid) {
        boolean cmtFlag = false;
        try {
            // update runtime record
            List<RenBinstepEntity> stepItems = renBinstepEntityDAO.findRenBinstepEntitiesByRtid(rtid);
            RenRuntimerecordEntity record = renRuntimerecordEntityDAO.findByRtid(rtid);
            if (record != null) {
                record.setInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
                renRuntimerecordEntityDAO.saveOrUpdate(record);
            }
            cmtFlag = true;
            // find root node
            RenBinstepEntity rootStep = stepItems.stream().filter(t -> CommonUtil.IsNullOrEmpty(t.getSupervisorId())).findFirst().get();
            String rootNodeId = rootStep.getNodeId();
            // recovery other node
            Stack<RenBinstepEntity> workStack = new Stack<>();
            workStack.push(rootStep);
            while (!workStack.isEmpty()) {
                RenBinstepEntity currentStep = workStack.pop();
                String currentNodeId = currentStep.getNodeId();
                BOInstance curBin = SerializationUtil.DeserializationBOInstanceByByteArray(currentStep.getBinlog());
                Evaluator curEvaluator = EvaluatorFactory.getEvaluator(curBin.getStateMachine());
                BOXMLExecutor curExecutor = new BOXMLExecutor(curEvaluator, new MultiStateMachineDispatcher(), new SimpleErrorReporter());
                curExecutor.NodeId = curExecutor.getExctx().NodeId = currentNodeId;
                curExecutor.RootNodeId = rootNodeId;
                curExecutor.setRootContext(curEvaluator.newContext(null));
                curExecutor.setRtid(rtid);
                if (record != null) {
                    curExecutor.setPid(record.getProcessId());
                }
                curExecutor.attachInstance(curBin);
                curExecutor.setNotifiableId(currentStep.getNotifiableId());
                curExecutor.resume(currentStep.getSupervisorId());
                List<RenBinstepEntity> currentChildren = stepItems.stream().filter(t -> t.getSupervisorId().equals(currentNodeId)).collect(Collectors.toList());
                for (RenBinstepEntity cc : currentChildren) {
                    workStack.push(cc);
                }
            }
            return true;
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Resume service entity step from DB failed, action rollback.",
                    SteadyStepService.class.getName(), LogLevelType.ERROR, rtid);
            return false;
        }
    }
}
