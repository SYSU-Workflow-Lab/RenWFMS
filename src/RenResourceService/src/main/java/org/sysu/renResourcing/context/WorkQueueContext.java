/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.context;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renCommon.enums.RSEventType;
import org.sysu.renCommon.enums.WorkQueueType;
import org.sysu.renCommon.enums.WorkitemResourcingStatusType;
import org.sysu.renResourcing.context.contextService.WorkitemContextService;
import org.sysu.renResourcing.dao.RenQueueitemsEntityDAO;
import org.sysu.renResourcing.dao.RenWorkitemEntityDAO;
import org.sysu.renResourcing.entity.RenQueueitemsEntity;
import org.sysu.renResourcing.entity.RenWorkitemEntity;
import org.sysu.renResourcing.entity.RenWorkqueueEntity;
import org.sysu.renResourcing.interfaceService.InterfaceE;
import org.sysu.renResourcing.utility.LogUtil;
import org.sysu.renResourcing.utility.SpringContextUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Rinkako
 * Date  : 2018/2/7
 * Usage : WorkQueueContext context is an encapsulation of RenWorkqueueEntity in a
 * convenient way for resourcing service.
 */
public class WorkQueueContext implements Serializable, RCacheablesContext {
    /**
     * Queue global id.
     */
    private String queueId;

    /**
     * Queue owner worker id, {@code GlobalContext.WORKQUEUE_ADMIN_PREFIX} if an admin queue.
     */
    private String ownerWorkerId;

    /**
     * Queue type enum.
     */
    private WorkQueueType type;

    /**
     * Workitem in this queue, map in pattern (workitemId, workitemObject)
     */
    private ConcurrentHashMap<String, WorkitemContext> workitems = new ConcurrentHashMap<>();

    /**
     * Add or update a workitem to this queue.
     *
     * @param workitem workitem entity.
     */
    public synchronized void AddOrUpdate(WorkitemContext workitem) {
        this.RefreshFromSteady();
        this.workitems.put(workitem.getEntity().getWid(), workitem);
        this.AddChangesToSteady(workitem);
        this.LogEvent(workitem);
    }

    /**
     * Add or update all entries of a work item queue Map.
     *
     * @param queueMap Map in pattern (workitemId, workitemObject)
     */
    public synchronized void AddQueue(Map<String, WorkitemContext> queueMap) {
        this.RefreshFromSteady();
        this.workitems.putAll(queueMap);
        this.AddChangesToSteady(new HashSet<>(queueMap.values()));
        this.LogEvent(queueMap);
    }

    /**
     * Add or update all item in the queue passed by another queue.
     * Entries in another queue will be copied and append to this queue.
     *
     * @param queue queue to be added
     */
    public synchronized void AddQueue(WorkQueueContext queue) {
        this.RefreshFromSteady();
        Map<String, WorkitemContext> qMap = queue.GetQueueAsMap();
        this.workitems.putAll(qMap);
        this.AddChangesToSteady(queue.GetQueueAsSet());
        this.LogEvent(qMap);
    }

    /**
     * Remove a workitem from this queue.
     *
     * @param workitemId workitem global id
     */
    public synchronized void Remove(String workitemId) {
        this.RefreshFromSteady();
        this.workitems.remove(workitemId);
        this.RemoveChangesToSteady(workitemId);
    }

    /**
     * Remove a workitem from this queue.
     *
     * @param workitem workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void Remove(WorkitemContext workitem) {
        this.RefreshFromSteady();
        String wid = workitem.getEntity().getWid();
        this.workitems.remove(wid);
        this.RemoveChangesToSteady(wid);
    }

    /**
     * Removes all entries in a queue.
     *
     * @param queue the queue of items to remove
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void RemoveQueue(WorkQueueContext queue) {
        this.RefreshFromSteady();
        Set<WorkitemContext> qSet = queue.GetQueueAsSet();
        HashSet<String> idSet = new HashSet<>();
        for (WorkitemContext ctx : qSet) {
            String wid = ctx.getEntity().getWid();
            this.Remove(wid);
            idSet.add(wid);
        }
        this.RemoveChangesToSteady(idSet);
    }

    /**
     * Remove all entries from a specific process runtime.
     *
     * @param rtid process rtid
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void RemoveByRtid(String rtid) {
        this.RefreshFromSteady();
        // clone queue prevent iteration fault
        Set<WorkitemContext> clonedQueue = new HashSet<>(this.workitems.values());
        HashSet<String> idSet = new HashSet<>();
        for (WorkitemContext workitem : clonedQueue) {
            if (workitem.getEntity().getRtid().equals(rtid)) {
                String wid = workitem.getEntity().getWid();
                this.workitems.remove(wid);
                idSet.add(wid);
            }
        }
        if (!idSet.isEmpty()) {
            this.RemoveChangesToSteady(idSet);
        }
    }

    /**
     * Check this queue is empty.
     *
     * @return true if queue empty
     */
    public synchronized boolean IsEmpty() {
        this.RefreshFromSteady();
        return this.workitems.isEmpty();
    }

    /**
     * Count the queue length.
     *
     * @return number of workitems in this queue
     */
    public synchronized int Count() {
        this.RefreshFromSteady();
        return this.workitems.size();
    }

    /**
     * Check if a workitem in queue.
     *
     * @param workitemId workitem global id
     * @return true if exist in queue
     */
    public synchronized boolean Contains(String workitemId) {
        this.RefreshFromSteady();
        return this.workitems.containsKey(workitemId);
    }

    /**
     * Get a workitem context from this queue by its global id.
     *
     * @param workitemId workitem global id
     * @return workitem context, null if not exist
     */
    public synchronized WorkitemContext Retrieve(String workitemId) {
        this.RefreshFromSteady();
        return this.workitems.get(workitemId);
    }

    /**
     * Clear the queue.
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void Clear() {
        this.RefreshFromSteady();
        HashSet<String> idSet = new HashSet<>();
        for (WorkitemContext ctx : this.workitems.values()) {
            idSet.add(ctx.getEntity().getWid());
        }
        this.workitems.clear();
        this.RemoveChangesToSteady(idSet);
    }

    /**
     * Get a concurrent hash map of all workitem in queue.
     *
     * @return all members of the queue as a HashMap of (workitemId, workitemObject)
     */
    public synchronized Map<String, WorkitemContext> GetQueueAsMap() {
        this.RefreshFromSteady();
        return new HashMap<>(this.workitems);
    }

    /**
     * Get a copied hash set of all workitem in queue.
     *
     * @return all members of the queue as a HashSet
     */
    public synchronized Set<WorkitemContext> GetQueueAsSet() {
        this.RefreshFromSteady();
        Set<WorkitemContext> retSet = new HashSet<>();
        for (WorkitemContext ctx : this.workitems.values()) {
            if (ctx != null) {
                retSet.add(ctx);
            }
        }
        return retSet;
    }

    /**
     * Get queue global id.
     *
     * @return global id string
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Get owner worker global id, {@code GlobalContext.WORKQUEUE_ADMIN_PREFIX} if an admin queue.
     *
     * @return global id string
     */
    public String getOwnerWorkerId() {
        return this.ownerWorkerId;
    }

    /**
     * Get queue type.
     *
     * @return queue type enum
     */
    public WorkQueueType getType() {
        return this.type;
    }

    /**
     * Write resource event log to entity.
     *
     * @param workitem workitem context
     */
    private void LogEvent(WorkitemContext workitem) {
        if (this.type != WorkQueueType.WORKLISTED) {
            RSEventType evtType = null;
            switch (this.type) {
                case OFFERED:
                    evtType = RSEventType.offer;
                    break;
                case ALLOCATED:
                    evtType = RSEventType.allocate;
                    break;
                case STARTED:
                    evtType = RSEventType.start;
                    break;
                case SUSPENDED:
                    evtType = RSEventType.suspend;
                    break;
                case UNOFFERED:
                    evtType = RSEventType.unoffer;
                    break;
            }
            assert evtType != null;
            InterfaceE interfaceE = (InterfaceE) SpringContextUtil.getBean("interfaceE");
            interfaceE.WriteLog(workitem, this.ownerWorkerId, evtType);
        }
    }

    /**
     * Write resource event log to entity.
     *
     * @param workitems workitem context map
     */
    private void LogEvent(Map<String, WorkitemContext> workitems) {
        for (WorkitemContext workitem : workitems.values()) {
            this.LogEvent(workitem);
        }
    }

    /**
     * Refresh remove workitem changes to entity.
     * NOTICE this method will be called when perform SET DATA type of queue context
     * to make sure data consistency among all RS.
     *
     * @param removeItemId id of context to remove
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void RemoveChangesToSteady(String removeItemId) {
        HashSet<String> oneSet = new HashSet<>();
        oneSet.add(removeItemId);
        this.RemoveChangesToSteady(oneSet);
    }

    /**
     * Refresh remove workitem changes to entity.
     * NOTICE this method will be called when perform SET DATA type of queue context
     * to make sure data consistency among all RS.
     *
     * @param removeSet id of contexts to remove
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void RemoveChangesToSteady(Set<String> removeSet) {
        if (this.type == WorkQueueType.WORKLISTED) {
            return;
        }
        try {
            StringBuilder ids = new StringBuilder();
            for (String workitemId : removeSet) {
                ids.append("'").append(workitemId).append("'").append(",");
            }
            String idStr = ids.toString();
            if (idStr.length() > 0) {
                idStr = idStr.substring(0, idStr.length() - 1);
            }
            RenQueueitemsEntityDAO renQueueitemsEntityDAO = (RenQueueitemsEntityDAO) SpringContextUtil.getBean("renQueueitemsEntityDAO");
            renQueueitemsEntityDAO.deleteByWorkqueueIdAndWorkitemId(this.queueId, idStr);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log(String.format("When WorkQueue(%s of %s, %s) flush remove changes to entity exception occurred, %s",
                    this.queueId, this.ownerWorkerId, this.type.name(), ex), WorkQueueContext.class.getName(),
                    LogLevelType.ERROR, "");
            throw ex;
        }
    }

    /**
     * Refresh add workitem changes to entity.
     * NOTICE this method will be called when perform SET DATA type of queue context
     * to make sure data consistency among all RS.
     *
     * @param addItem context to add
     */
    public synchronized void AddChangesToSteady(WorkitemContext addItem) {
        HashSet<WorkitemContext> oneSet = new HashSet<>();
        oneSet.add(addItem);
        this.AddChangesToSteady(oneSet);
    }

    /**
     * Refresh add workitem changes to entity.
     * NOTICE this method will be called when perform SET DATA type of queue context
     * to make sure data consistency among all RS.
     *
     * @param addSet set of context to add
     */
    public synchronized void AddChangesToSteady(Set<WorkitemContext> addSet) {
        if (this.type == WorkQueueType.WORKLISTED) {
            return;
        }
        try {
            RenQueueitemsEntityDAO renQueueitemsEntityDAO = (RenQueueitemsEntityDAO) SpringContextUtil.getBean("renQueueitemsEntityDAO");
            for (WorkitemContext workitem : addSet) {
                String workitemId = workitem.getEntity().getWid();
                RenQueueitemsEntity rqe = renQueueitemsEntityDAO.findByWorkqueueIdAndWorkitemId(this.queueId, workitemId);
                if (rqe == null) {
                    rqe = new RenQueueitemsEntity();
                    rqe.setWorkitemId(workitemId);
                    rqe.setWorkqueueId(this.queueId);
                    renQueueitemsEntityDAO.saveOrUpdate(rqe);
                }
            }
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log(String.format("When WorkQueue(%s of %s, %s) flush add changes to entity exception occurred, %s",
                    this.queueId, this.ownerWorkerId, this.type.name(), ex), WorkQueueContext.class.getName(),
                    LogLevelType.ERROR, "");
            throw ex;
        }
    }

    /**
     * Refresh work queue from entity.
     * NOTICE this method will be called when perform GET DATA type of queue context
     * to make sure data consistency among all RS.
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public synchronized void RefreshFromSteady() {
        // TODO: whether WORKLISTED is auto-generated or not is not clear. Here we generate by add up 4 queue.
        if (this.type == WorkQueueType.WORKLISTED) {
            return;
        }
        boolean cmtFlag = false;
        try {
            ConcurrentHashMap<String, WorkitemContext> newMaps = new ConcurrentHashMap<>();
            WorkitemContextService workitemContextService = (WorkitemContextService) SpringContextUtil.getBean("workitemContextService");
            if (this.type == WorkQueueType.WORKLISTED) {  // TODO: never reach here
                RenWorkitemEntityDAO renWorkitemEntityDAO = (RenWorkitemEntityDAO) SpringContextUtil.getBean("renWorkitemEntityDAO");
                List<RenWorkitemEntity> allActiveItems = renWorkitemEntityDAO.findRenWorkitemEntitiesByFourStatus(
                        WorkitemResourcingStatusType.Allocated.name(),
                        WorkitemResourcingStatusType.Offered.name(),
                        WorkitemResourcingStatusType.Started.name(),
                        WorkitemResourcingStatusType.Suspended.name());
                cmtFlag = true;
                // worklisted queue owner worker id is directly equals to admin auth name
//                String myDomain = AuthDomainHelper.GetDomainByAuthName(this.ownerWorkerId);
//                for (RenWorkitemEntity workitemEntity : allActiveItems) {
//                    String authDomain = AuthDomainHelper.GetDomainByRTID(workitemEntity.getRtid());
//                    if (myDomain.equals(authDomain)) {
//                        WorkitemContext workitem = WorkitemContext.GetContext(workitemEntity.getWid(), workitemEntity.getRtid());
//                        newMaps.put(workitemEntity.getWid(), workitem);
//                    }
//                }
                for (RenWorkitemEntity workitemEntity : allActiveItems) {
                    WorkitemContext workitem = workitemContextService.GetContext(workitemEntity.getWid(), workitemEntity.getRtid());
                    newMaps.put(workitemEntity.getWid(), workitem);
                }
            } else {
                RenQueueitemsEntityDAO renQueueitemsEntityDAO = (RenQueueitemsEntityDAO) SpringContextUtil.getBean("renQueueitemsEntityDAO");
                List<RenQueueitemsEntity> inSteady = (ArrayList<RenQueueitemsEntity>) renQueueitemsEntityDAO.findRenQueueitemsEntitiesByWorkqueueId(this.queueId);
                cmtFlag = true;
                for (RenQueueitemsEntity rqe : inSteady) {
                    WorkitemContext workitem = workitemContextService.GetContext(rqe.getWorkitemId(), "#RS_INTERNAL_" + GlobalContext.RESOURCE_SERVICE_GLOBAL_ID);
                    newMaps.put(rqe.getWorkitemId(), workitem);
                }
            }
            this.workitems = newMaps;
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log(String.format("When WorkQueue(%s of %s, %s) refresh from entity exception occurred, %s",
                    this.queueId, this.ownerWorkerId, this.type.name(), ex), WorkQueueContext.class.getName(),
                    LogLevelType.ERROR, "");
            throw ex;
        }
    }

    /**
     * Generate a context by its entity entity.
     *
     * @param workqueueEntity WorkQueue entity
     * @return WorkQueue context
     */
    public static WorkQueueContext GenerateContext(RenWorkqueueEntity workqueueEntity) {
        assert workqueueEntity != null;
        return new WorkQueueContext(workqueueEntity.getQueueId(),
                workqueueEntity.getOwnerId(), WorkQueueType.values()[workqueueEntity.getType()]);
    }


    /**
     * Create a new work queue context.
     * NOTICE that usually this method should not be called unless <b>worklisted</b> queue.
     *
     * @param id            work queue global id
     * @param ownerWorkerId owner worker global id
     * @param type          queue type enum
     */
    public WorkQueueContext(String id, String ownerWorkerId, WorkQueueType type) {
        this.queueId = id;
        this.ownerWorkerId = ownerWorkerId;
        this.type = type;
        // here no need for queued workitem refresh, any GET methods will refresh automatically.
    }
}
