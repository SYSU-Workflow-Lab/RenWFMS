package org.sysu.renResourcing.context.contextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.entity.RenBoEntity;
import org.sysu.renCommon.entity.RenRstaskEntity;
import org.sysu.renCommon.entity.RenWorkitemEntity;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.enums.WorkitemResourcingStatusType;
import org.sysu.renCommon.enums.WorkitemStatusType;
import org.sysu.renCommon.utility.SerializationUtil;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renResourcing.consistency.ContextCachePool;
import org.sysu.renResourcing.context.TaskContext;
import org.sysu.renResourcing.context.WorkitemContext;
import org.sysu.renResourcing.dao.RenBoEntityDAO;
import org.sysu.renResourcing.dao.RenQueueitemsEntityDAO;
import org.sysu.renResourcing.dao.RenRstaskEntityDAO;
import org.sysu.renResourcing.dao.RenWorkitemEntityDAO;
import org.sysu.renCommon.entity.RenQueueitemsEntity;
import org.sysu.renResourcing.interfaceService.InterfaceA;
import org.sysu.renResourcing.utility.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Skye on 2018/12/22.
 * <p>
 * Usage : WorkitemContext Handler.
 */

@Service
public class WorkitemContextService {

    /**
     * TaskContext Handler
     */
    @Autowired
    private TaskContextService taskContextService;

    @Autowired
    private RenQueueitemsEntityDAO renQueueitemsEntityDAO;

    @Autowired
    private RenWorkitemEntityDAO renWorkitemEntityDAO;

    @Autowired
    private RenRstaskEntityDAO renRstaskEntityDAO;

    @Autowired
    private RenBoEntityDAO renBoEntityDAO;

    @Autowired
    private InterfaceA interfaceA;

    /**
     * Generate a user-friendly workitem package.
     *
     * @param workitem workitem context
     * @return HashMap of workitem descriptor
     */
    @Transactional(rollbackFor = Exception.class)
    public HashMap<String, String> GenerateResponseWorkitem(WorkitemContext workitem) {
        HashMap<String, String> retMap = new HashMap<>();
        RenWorkitemEntity entity = workitem.getEntity();
        String workitemId = entity.getWid();
        retMap.put("Wid", workitemId);
        retMap.put("Rtid", entity.getRtid());
        retMap.put("CallbackNodeId", entity.getCallbackNodeId());
        retMap.put("TaskName", workitem.getTaskContext().getTaskName());
        retMap.put("TaskId", workitem.getTaskContext().getTaskId());
        retMap.put("Role", workitem.getTaskContext().getBrole());
        retMap.put("Documentation", workitem.getTaskContext().getDocumentation());
        retMap.put("Argument", SerializationUtil.JsonSerialization(workitem.getArgsDict()));
        retMap.put("Status", entity.getStatus());
        retMap.put("ResourceStatus", entity.getResourceStatus());
        retMap.put("EnablementTime", entity.getEnablementTime() == null ? "" : entity.getEnablementTime().toString());
        retMap.put("FiringTime", entity.getFiringTime() == null ? "" : entity.getFiringTime().toString());
        retMap.put("StartTime", entity.getStartTime() == null ? "" : entity.getStartTime().toString());
        retMap.put("CompletionTime", entity.getCompletionTime() == null ? "" : entity.getCompletionTime().toString());
        retMap.put("ExecuteTime", String.valueOf(entity.getExecuteTime()));
        List<RenQueueitemsEntity> relations = null;
        try {
            relations = renQueueitemsEntityDAO.findRenQueueitemsEntitiesByWorkitemId(workitemId);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("GenerateResponseWorkitem but cannot read relation from entity, " + ex,
                    WorkitemContextService.class.getName(), LogLevelType.ERROR, entity.getRtid());
        }
        if (relations == null) {
            retMap.put("WorkerIdList", "[]");
        } else {
            StringBuilder workerIdSb = new StringBuilder();
            workerIdSb.append("[");
            for (RenQueueitemsEntity rqe : relations) {
                String[] workqueueIdItem = rqe.getWorkqueueId().split("_");
                String workerId;
                if (workqueueIdItem.length == 4) {
                    workerId = String.format("\"%s_%s\"", workqueueIdItem[2], workqueueIdItem[3]);
                }
                // for admin queue
                else {
                    workerId = String.format("\"%s\"", workqueueIdItem[2]);
                }
                workerIdSb.append(workerId).append(",");
            }
            String workerIdList = workerIdSb.toString();
            if (workerIdList.length() > 1) {
                workerIdList = workerIdList.substring(0, workerIdList.length() - 1);
            }
            workerIdList += "]";
            retMap.put("WorkerIdList", workerIdList);
        }
        return retMap;
    }

    /**
     * Generate a list of user-friendly workitem packages.
     *
     * @param wList      list of workitem context
     * @param onlyActive whether only get active workitems
     * @return ArrayList of HashMap of workitem descriptor
     */
    public ArrayList<HashMap<String, String>> GenerateResponseWorkitems(ArrayList<WorkitemContext> wList, boolean onlyActive) {
        ArrayList<HashMap<String, String>> retList = new ArrayList<>();
        for (WorkitemContext workitem : wList) {
            String status = workitem.getEntity().getStatus();
            if (onlyActive && (status.equals(WorkitemStatusType.Complete.name())
                    || status.equals(WorkitemStatusType.ForcedComplete.name())
                    || status.equals(WorkitemStatusType.Discarded.name()))) {
                continue;
            }
            retList.add(this.GenerateResponseWorkitem(workitem));
        }
        return retList;
    }

    /**
     * Get Workitem Context by RTID.
     *
     * @param rtid process rtid
     * @return ArrayList of workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public ArrayList<WorkitemContext> GetContextRTID(String rtid) {
        ArrayList<WorkitemContext> retList = new ArrayList<>();
        try {
            List<RenWorkitemEntity> workitems = renWorkitemEntityDAO.findRenWorkitemEntitiesByRtid(rtid);
            for (RenWorkitemEntity rwe : workitems) {
                retList.add(this.GetContext(rwe.getWid(), rwe.getRtid()));
            }
            return retList;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Get Workitem Context by Domain.
     *
     * @param domain domain name
     * @return ArrayList of workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public ArrayList<WorkitemContext> GetContextInDomain(String domain) {
        ArrayList<WorkitemContext> retList = new ArrayList<>();
        try {
            List<RenWorkitemEntity> workitems = renWorkitemEntityDAO.findRenWorkitemEntitiesByDomain("@" + domain + "_");
            for (RenWorkitemEntity rwe : workitems) {
                retList.add(this.GetContext(rwe.getWid(), rwe.getRtid()));
            }
            return retList;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Get an exist workitem context from cache or entity.
     *
     * @param wid workitem global id
     * @return workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkitemContext GetContext(String wid, String rtid) {
        return this.GetContext(wid, rtid, false);
//        return null;
    }

    /**
     * Get an exist workitem context.
     *
     * @param wid         workitem global id
     * @param rtid        process rtid
     * @param forceReload force reload from entity and refresh cache
     * @return workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkitemContext GetContext(String wid, String rtid, boolean forceReload) {
        WorkitemContext cachedCtx = ContextCachePool.Retrieve(WorkitemContext.class, wid);
        // fetch cache
        if (cachedCtx != null && !forceReload) {
            return cachedCtx;
        }
        boolean cmtFlag = false;
        try {
            RenWorkitemEntity rwe = renWorkitemEntityDAO.findByWid(wid);
            if (rwe == null) {
                throw new RuntimeException("RenWorkitemEntity is NULL!");
            }
            String taskId = rwe.getTaskid();
            RenRstaskEntity rte = renRstaskEntityDAO.findByTaskid(taskId);
            String taskName = rte.getPolymorphismName();
            String boId = rte.getBoid();
            RenBoEntity rbe = renBoEntityDAO.findByBoId(boId);
            String boName = rbe.getBoName();
            cmtFlag = true;
            WorkitemContext retCtx = new WorkitemContext();
            retCtx.setEntity(rwe);
            retCtx.setArgsDict(SerializationUtil.JsonDeserialization(rwe.getArguments(), HashMap.class));
            retCtx.setTaskContext(taskContextService.GetContext(rwe.getRtid(), boName, taskName));
            ContextCachePool.AddOrUpdate(wid, retCtx);
            return retCtx;
        } catch (Exception ex) {
            if (!cmtFlag) {
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Get workitem context but exception occurred, " + ex,
                    WorkitemContextService.class.getName(), LogLevelType.ERROR, rtid);
            throw ex;
        }
    }

    /**
     * Generate a workitem context and save it to entity by a task context.
     *
     * @param taskContext    task context to be the generation template
     * @param rtid           process rtid
     * @param args           parameter-arguments map
     * @param callbackNodeId producer instance tree node global id for callback
     * @return workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkitemContext GenerateContext(TaskContext taskContext, String rtid, HashMap args, String callbackNodeId) throws Exception {
        assert args != null && taskContext.getParameters() != null;
        //HashMap parameterMap = SerializationUtil.JsonDeserialization(taskContext.getParameters(), HashMap.class);
        if (args.size() != taskContext.getParameters().size()) {
            LogUtil.Log(String.format("Generate workitem for task %s, but arguments(%s) and parameters(%s) not equal",
                    taskContext.getTaskName(), args.size(), taskContext.getParameters().size()),
                    WorkitemContextService.class.getName(), LogLevelType.WARNING, rtid);
        }
        boolean cmtFlag = false;
        try {
            RenWorkitemEntity rwe = new RenWorkitemEntity();
            rwe.setWid(String.format("WI_%s", UUID.randomUUID().toString()));
            rwe.setRtid(rtid);
            rwe.setResourcingId(GlobalContext.RESOURCE_SERVICE_GLOBAL_ID);
            rwe.setProcessId(taskContext.getPid());
            rwe.setBoId(taskContext.getBoid());
            rwe.setTaskid(taskContext.getTaskGlobalId());
            rwe.setTaskPolymorphismId(taskContext.getTaskId());
            rwe.setStatus(WorkitemStatusType.Enabled.name());
            rwe.setResourceStatus(WorkitemResourcingStatusType.Unoffered.name());
            rwe.setExecuteTime(0L);
            rwe.setCallbackNodeId(callbackNodeId);
            rwe.setEnablementTime(TimestampUtil.GetCurrentTimestamp());
            rwe.setArguments(SerializationUtil.JsonSerialization(args));
            renWorkitemEntityDAO.saveOrUpdate(rwe);
            cmtFlag = true;
            WorkitemContext wCtx = new WorkitemContext();
            wCtx.setEntity(rwe);
            wCtx.setArgsDict((HashMap<String, String>) args);
            wCtx.setTaskContext(taskContext);
            // handle callback and hook
            interfaceA.HandleCallbackAndHook(WorkitemStatusType.Enabled, wCtx, taskContext, null);
            return wCtx;
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Generate workitem context but exception occurred, " + ex,
                    WorkitemContextService.class.getName(), LogLevelType.ERROR, rtid);
            throw ex;
        }
    }

    /**
     * Save changes context to entity memory.
     *
     * @param context context to be saved
     */
    @Transactional(rollbackFor = Exception.class)
    public void SaveToSteady(WorkitemContext context) {
        if (context == null) {
            LogUtil.Log("Ignore null workitem context saving.", WorkitemContextService.class.getName(),
                    LogLevelType.WARNING, "");
            return;
        }
        try {
            renWorkitemEntityDAO.saveOrUpdate(context.getEntity());
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Save workitem context but exception occurred, " + ex,
                    WorkitemContextService.class.getName(), LogLevelType.ERROR, context.getEntity().getRtid());
        }
    }

}
