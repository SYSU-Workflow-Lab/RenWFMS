package org.sysu.renResourcing.context.contextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.enums.WorkQueueType;
import org.sysu.renResourcing.context.WorkQueueContext;
import org.sysu.renResourcing.context.WorkitemContext;
import org.sysu.renResourcing.dao.RenQueueitemsEntityDAO;
import org.sysu.renResourcing.dao.RenWorkqueueEntityDAO;
import org.sysu.renCommon.entity.RenWorkqueueEntity;
import org.sysu.renResourcing.utility.LogUtil;

/**
 * Created by Skye on 2018/12/22.
 * <p>
 * Usage : WorkQueueContext Handler.
 */

@Service
public class WorkQueueContextService {

    @Autowired
    private RenWorkqueueEntityDAO renWorkqueueEntityDAO;

    @Autowired
    private RenQueueitemsEntityDAO renQueueitemsEntityDAO;

    /**
     * Get the specific queue context and store to entity.
     *
     * @param ownerWorkerId queue owner worker id
     * @param queueType     queue type enum
     * @return a workqueue context
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized WorkQueueContext GetContext(String ownerWorkerId, WorkQueueType queueType) {
        String wqid = String.format("WQ_%s_%s", queueType.name(), ownerWorkerId);
        boolean cmtFlag = false;
        try {
            RenWorkqueueEntity rwqe = renWorkqueueEntityDAO.findByOwnerIdAndType(ownerWorkerId, queueType.ordinal());
            // if not exist in entity then create a new one
            if (rwqe == null) {
                rwqe = new RenWorkqueueEntity();
                rwqe.setQueueId(wqid);
                rwqe.setOwnerId(ownerWorkerId);
                rwqe.setType(queueType.ordinal());
                renWorkqueueEntityDAO.saveOrUpdate(rwqe);
            }
            cmtFlag = true;
            return WorkQueueContext.GenerateContext(rwqe);
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log(String.format("Get WorkQueueContext (owner: %s, type: %s) exception occurred, %s", ownerWorkerId, queueType.name(), ex),
                    WorkQueueContextService.class.getName(), LogLevelType.ERROR, "");
            throw ex;
        }
    }

    /**
     * Remove a workitem from all queue.
     *
     * @param workitem workitem context
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void RemoveFromAllQueue(WorkitemContext workitem) {
        if (workitem == null) {
            return;
        }
        try {
            renQueueitemsEntityDAO.deleteByWorkitemId(workitem.getEntity().getWid());
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log(String.format("When RemoveFromAllQueue(%s) refresh from entity exception occurred, %s",
                    workitem.getEntity().getWid(), ex), WorkQueueContextService.class.getName(),
                    LogLevelType.ERROR, workitem.getEntity().getRtid());
        }
    }

}
