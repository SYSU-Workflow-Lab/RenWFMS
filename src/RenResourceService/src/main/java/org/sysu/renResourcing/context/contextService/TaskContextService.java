package org.sysu.renResourcing.context.contextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renResourcing.consistency.ContextCachePool;
import org.sysu.renResourcing.context.TaskContext;
import org.sysu.renResourcing.dao.RenBoEntityDAO;
import org.sysu.renResourcing.dao.RenRstaskEntityDAO;
import org.sysu.renResourcing.dao.RenRuntimerecordEntityDAO;
import org.sysu.renResourcing.entity.RenBoEntity;
import org.sysu.renResourcing.entity.RenRstaskEntity;
import org.sysu.renResourcing.entity.RenRuntimerecordEntity;
import org.sysu.renResourcing.utility.LogUtil;

/**
 * Created by Skye on 2018/12/22.
 *
 * Usage : TaskContext Handler.
 */

@Service
public class TaskContextService {

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenBoEntityDAO renBoEntityDAO;

    @Autowired
    private RenRstaskEntityDAO renRstaskEntityDAO;

    /**
     * Get a task context by its name and belonging BO name of one runtime.
     * @param rtid runtime record id
     * @param boName belong to BO id
     * @param taskName task name
     * @return Task resourcing context, null if exception occurred or assertion error
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskContext GetContext(String rtid, String boName, String taskName) {
        return this.GetContext(rtid, boName, taskName, false);
    }

    /**
     * Get a task context by its name and belonging BO name of one runtime.
     * @param rtid runtime record id
     * @param boName belong to BO id
     * @param taskName task name
     * @return Task resourcing context, null if exception occurred or assertion error
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskContext GetContext(String rtid, String boName, String taskName, boolean forceReload) {
        String taskCtxId = String.format("%s_%s_%s", rtid, boName, taskName);
        TaskContext cachedCtx = ContextCachePool.Retrieve(TaskContext.class, taskCtxId);
        // fetch cache
        if (cachedCtx != null && !forceReload) {
            return cachedCtx;
        }
        boolean cmtFlag = false;
        try {
            RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
            if (rre == null) {
                throw new RuntimeException("RenRuntimerecordEntity is NULL!");
            }
            String pid = rre.getProcessId();
            RenBoEntity rbe = renBoEntityDAO.findByPidAndBoName(pid, boName);
            if (rbe == null) {
                throw new RuntimeException("RenBoEntity is NULL!");
            }
            RenRstaskEntity taskEntity = renRstaskEntityDAO.findByBoidAndAndPolymorphismName(rbe.getBoid(), taskName);
            if (taskEntity == null) {
                throw new RuntimeException("RenRstaskEntity is NULL!");
            }
            cmtFlag = true;
            TaskContext generatedCtx = TaskContext.GenerateTaskContext(taskEntity, pid);
            ContextCachePool.AddOrUpdate(taskCtxId, generatedCtx);
            return generatedCtx;
        }
        catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("When json serialization exception occurred, transaction rollback. " + ex,
                    TaskContextService.class.getName(), LogLevelType.ERROR, rtid);
            return null;
        }
    }

}
