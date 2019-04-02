/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.workflow.*;
import org.sysu.workflow.core.BOXMLExecutor;
import org.sysu.workflow.core.Context;
import org.sysu.workflow.core.Evaluator;
import org.sysu.workflow.core.EvaluatorFactory;
import org.sysu.workflow.dao.*;
import org.sysu.renCommon.entity.*;
import org.sysu.workflow.core.env.MultiStateMachineDispatcher;
import org.sysu.workflow.core.env.SimpleErrorReporter;
import org.sysu.workflow.core.instanceTree.InstanceManager;
import org.sysu.workflow.core.instanceTree.RInstanceTree;
import org.sysu.workflow.core.instanceTree.RTreeNode;
import org.sysu.workflow.core.io.BOXMLReader;
import org.sysu.workflow.core.model.EnterableState;
import org.sysu.workflow.core.model.SCXML;
import org.sysu.workflow.core.model.extend.Task;
import org.sysu.workflow.core.model.extend.Tasks;
import org.sysu.workflow.utility.LogUtil;
import org.sysu.workflow.utility.SerializationUtil;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;


/**
 * Author: Ariana, Rinkako
 * Date  : 2018/1/22
 * Usage : Methods for processes runtime management.
 */

@Service
public class RuntimeManagementService {

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenProcessEntityDAO renProcessEntityDAO;

    @Autowired
    private RenBoEntityDAO renBoEntityDAO;

    @Autowired
    private RenRstaskEntityDAO renRstaskEntityDAO;

    @Autowired
    private RenArchivedTreeEntityDAO renArchivedTreeEntityDAO;

    /**
     * obtain main bo xml content from database according to the process id, and then read and execute it
     *
     * @param rtid the runtime record of a process
     */
    @Transactional(rollbackFor = Exception.class)
    public void LaunchProcess(String rtid) {
        boolean cmtFlag = false;
        try {
            RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
            if (rre == null) {
                throw new RuntimeException("RenRuntimerecordEntity:" + rtid + " is NULL!");
            }
            String pid = rre.getProcessId();
            rre.setInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
            renRuntimerecordEntityDAO.saveOrUpdate(rre);
            RenProcessEntity rpe = renProcessEntityDAO.findByPid(pid);
            if (rpe == null) {
                throw new RuntimeException("RenProcessEntity:" + pid + " is NULL!");
            }
            String mainBO = rpe.getMainBo();
            List<RenBoEntity> boList = renBoEntityDAO.findRenBoEntitiesByPid(pid);
            RenBoEntity mainBoEntity = null;
            for (RenBoEntity bo : boList) {
                RenBoEntity boEntity = bo;
                if (boEntity.getBoName().equals(mainBO)) {
                    mainBoEntity = boEntity;
                    break;
                }
            }
            cmtFlag = true;
            if (mainBoEntity == null) {
                LogUtil.Log("Main BO not exist for launching process: " + rtid,
                        RuntimeManagementService.class.getName(), LogLevelType.ERROR, rtid);
                return;
            }
            byte[] serializedBO = mainBoEntity.getSerialized();
            SCXML DeserializedBO = SerializationUtil.DeserializationSCXMLByByteArray(serializedBO);
            this.ExecuteBO(DeserializedBO, rtid, pid);
        } catch (Exception e) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("When read bo by rtid, exception occurred, " + e.toString() + ", service rollback",
                    RuntimeManagementService.class.getName(), LogLevelType.ERROR, rtid);
        }
    }

    /**
     * Serialize a list of BO by their id and return involved business role names.
     *
     * @param boidList BOs to be serialized
     * @return HashSet of Involved business role name
     */
    @Transactional(rollbackFor = Exception.class)
    public HashSet<String> SerializeBO(String boidList) {
        HashSet<String> retSet = new HashSet<>();
        try {
            String[] boidItems = boidList.split(",");
            for (String boid : boidItems) {
                RenBoEntity rbe = renBoEntityDAO.findByBoId(boid);
                if (rbe == null) {
                    throw new NullPointerException("RenBoEntity is not found");
                }
                SCXML scxml = this.ParseStringToSCXML(rbe.getBoContent());
                if (scxml == null) {
                    continue;
                }
                HashSet<String> oneInvolves = this.GetInvolvedBusinessRole(scxml);
                retSet.addAll(oneInvolves);
                rbe.setBroles(SerializationUtil.JsonSerialization(oneInvolves, ""));
                rbe.setSerialized(SerializationUtil.SerializationSCXMLToByteArray(scxml));
                Tasks tasks = scxml.getTasks();
                for (Task t : tasks.getTaskList()) {
                    AbstractMap.SimpleEntry<String, String> heDesc = t.GenerateCallbackDescriptor();
                    RenRstaskEntity rrte = new RenRstaskEntity();
                    rrte.setBoid(boid);
                    rrte.setTaskid(String.format("TSK_%s", UUID.randomUUID().toString()));
                    rrte.setHookdescriptor(heDesc.getKey());
                    rrte.setEventdescriptor(heDesc.getValue());
                    rrte.setDocumentation(t.getDocumentation());
                    rrte.setPrinciple(t.getPrinciple().GenerateDescriptor());
                    rrte.setPolymorphismId(t.getId());
                    rrte.setPolymorphismName(t.getName());
                    rrte.setBrole(t.getBrole());
                    rrte.setParameters(t.GenerateParamDescriptor());
                    renRstaskEntityDAO.saveOrUpdate(rrte);
                }
                renBoEntityDAO.saveOrUpdate(rbe);
            }
            return retSet;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log(String.format("When serialize BOList(%s), exception occurred, %s, service rollback", boidList, ex),
                    RuntimeManagementService.class.getName(), LogLevelType.ERROR, boidList);
        }
        return retSet;
    }

    /**
     * Get a user-friendly descriptor of an instance tree.
     *
     * @param rtid process runtime record id
     * @return a descriptor of span instance tree JSON descriptor
     */
    @Transactional(rollbackFor = Exception.class)
    public String GetSpanTreeDescriptor(String rtid) {
        RInstanceTree tree = InstanceManager.GetInstanceTree(rtid, false);
        if (tree == null || tree.Root == null) {
            RenArchivedTreeEntity rate = null;
            try {
                rate = renArchivedTreeEntityDAO.findByRtid(rtid);
            } catch (Exception ex) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            if (rate == null) {
                return null;
            }
            return rate.getTree();
        }
        FriendlyTreeNode rootFNode = new FriendlyTreeNode();
        this.Nephren(tree.Root, rootFNode);
        return SerializationUtil.JsonSerialization(rootFNode, rtid);
    }

    /**
     * Recursively handle span the user-friendly package tree of a specific instance tree.
     * This method is to commemorate a girl devoted her love to guard the happiness of who she loved and his lover. -RK
     *
     * @param node  current span root node
     * @param fNode user-friendly package node of current span node
     */
    private void Nephren(@NotNull RTreeNode node, @NotNull FriendlyTreeNode fNode) {
        fNode.BOName = node.getExect().getScInstance().getStateMachine().getName();
        fNode.GlobalId = node.getGlobalId();
        fNode.NotifiableId = node.getExect().NotifiableId;
        Set<EnterableState> status = node.getExect().getScInstance().getCurrentStatus().getActiveStates();
        HashSet<String> stringSet = new HashSet<>();
        for (EnterableState st : status) {
            stringSet.add(st.getId());
        }
        fNode.StatusDescriptor = SerializationUtil.JsonSerialization(stringSet, node.getExect().Rtid);
        for (RTreeNode sub : node.Children) {
            FriendlyTreeNode subFn = new FriendlyTreeNode();
            this.Nephren(sub, subFn);
            fNode.Children.add(subFn);
        }
    }

    /**
     * execute the main bo of the current process
     *
     * @param scxml scxml instance
     * @param rtid  process rtid
     * @param pid   process global id
     */
    private void ExecuteBO(SCXML scxml, String rtid, String pid) {
        try {
            Evaluator evaluator = EvaluatorFactory.getEvaluator(scxml);
            BOXMLExecutor executor = new BOXMLExecutor(evaluator, new MultiStateMachineDispatcher(), new SimpleErrorReporter());
            Context rootContext = evaluator.newContext(null);
            executor.setRootContext(rootContext);
            executor.setRtid(rtid);
            executor.setPid(pid);
            executor.setStateMachine(scxml);
            executor.go();
        } catch (Exception e) {
            LogUtil.Log("When ExecuteBO, exception occurred, " + e.toString(),
                    RuntimeManagementService.class.getName(), LogLevelType.ERROR, rtid);
        }
    }

    /**
     * Interpret XML string to SCXML instance.
     *
     * @param boXMLContent BO XML string
     * @return {@code SCXML} instance
     */
    private SCXML ParseStringToSCXML(String boXMLContent) {
        try {
            InputStream inputStream = new ByteArrayInputStream(boXMLContent.getBytes());
            return BOXMLReader.read(inputStream);
        } catch (Exception ex) {
            LogUtil.Log(String.format("When read BO XML data, exception occurred, %s", ex),
                    RuntimeManagementService.class.getName(), LogLevelType.ERROR, boXMLContent);
        }
        return null;
    }

    /**
     * Get involved business role name of one BO.
     *
     * @param scxml BO {@code SCXML} instance.
     * @return HashSet of involved business role name
     */
    private HashSet<String> GetInvolvedBusinessRole(SCXML scxml) {
        HashSet<String> retSet = new HashSet<String>();
        ArrayList<Task> taskList = scxml.getTasks().getTaskList();
        for (Task task : taskList) {
            retSet.add(task.getBrole());
        }
        return retSet;
    }

    /**
     * A class for user-friendly tree node data package.
     */
    private class FriendlyTreeNode {

        public String NotifiableId;

        public String GlobalId;

        public String BOName;

        public String StatusDescriptor;

        public ArrayList<FriendlyTreeNode> Children = new ArrayList<>();
    }
}