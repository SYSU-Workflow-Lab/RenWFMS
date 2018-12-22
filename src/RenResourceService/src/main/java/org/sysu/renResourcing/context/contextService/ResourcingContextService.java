package org.sysu.renResourcing.context.contextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.enums.RServiceType;
import org.sysu.renCommon.utility.SerializationUtil;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renResourcing.consistency.ContextCachePool;
import org.sysu.renResourcing.context.ResourcingContext;
import org.sysu.renResourcing.context.TaskContext;
import org.sysu.renResourcing.dao.RenRsrecordEntityDAO;
import org.sysu.renResourcing.entity.RenRsrecordEntity;
import org.sysu.renResourcing.utility.LogUtil;

import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by Skye on 2018/12/22.
 *
 * Usage : ResourcingContext Handler.
 */

@Service
public class ResourcingContextService {

    @Autowired
    private RenRsrecordEntityDAO renRsrecordEntityDAO;

    /**
     * Get a resourcing request context.
     * @param rstid resourcing request global id, null if create a new one
     * @param rtid process rtid
     * @param service service type enum
     * @param argsDict service argument dict
     * @return Resourcing request context, null if exception occurred or assertion error
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourcingContext GetContext(String rstid, String rtid, RServiceType service, Hashtable<String, Object> argsDict) {
        return this.GetContext(rstid, rtid, service, argsDict, false);
    }

    /**
     * Get a resourcing request context.
     * @param rstid resourcing request global id, null if create a new one
     * @param rtid process rtid
     * @param service service type enum
     * @param argsDict service argument dict
     * @param forceReload force reload from entity and refresh cache
     * @return Resourcing request context, null if exception occurred or assertion error
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourcingContext GetContext(String rstid, String rtid, RServiceType service, Hashtable<String, Object> argsDict, boolean forceReload) {
        if (rstid != null && !forceReload) {
            ResourcingContext cachedCtx = ContextCachePool.Retrieve(ResourcingContext.class, rstid);
            // fetch cache
            if (cachedCtx != null) {
                return cachedCtx;
            }
        }
        boolean cmtFlag = false;
        try {
            RenRsrecordEntity renRsrecordEntity;
            // create new
            if (rstid == null) {
                renRsrecordEntity = new RenRsrecordEntity();
                rstid = String.format("RSR_%s", UUID.randomUUID().toString());
                renRsrecordEntity.setRstid(rstid);
                renRsrecordEntity.setRtid(rtid);
                renRsrecordEntity.setPriority(0);
                renRsrecordEntity.setReceiveTimestamp(TimestampUtil.GetCurrentTimestamp());
                renRsrecordEntity.setResourcingId(GlobalContext.RESOURCE_SERVICE_GLOBAL_ID);
                renRsrecordEntity.setService(service.name());
                renRsrecordEntity.setArgs(SerializationUtil.JsonSerialization(argsDict));
                renRsrecordEntityDAO.saveOrUpdate(renRsrecordEntity);
                cmtFlag = true;
            }
            // exist from entity
            else {
                renRsrecordEntity = renRsrecordEntityDAO.findByRstid(rstid);
                assert renRsrecordEntity != null;
                cmtFlag = true;
            }
            ResourcingContext generatedCtx = ResourcingContext.GenerateResourcingContext(renRsrecordEntity);
            ContextCachePool.AddOrUpdate(rstid, generatedCtx);
            return generatedCtx;
        }
        catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Get resourcing request context but exception occurred, " + ex,
                    TaskContext.class.getName(), LogLevelType.ERROR, rtid);
            return null;
        }
    }

    /**
     * Save changes context to entity memory.
     * @param context context to be saved
     */
    @Transactional(rollbackFor = Exception.class)
    public void SaveToSteady(ResourcingContext context) {
        if (context == null) {
            LogUtil.Log("Ignore null resourcing context saving.", TaskContext.class.getName(),
                    LogLevelType.WARNING, "");
            return;
        }
        try {
            RenRsrecordEntity rre = renRsrecordEntityDAO.findByRstid(context.getRstid());
            assert rre != null;
            rre.setReceiveTimestamp(context.getReceivedTimestamp());
            rre.setScheduledTimestamp(context.getScheduledTimestamp());
            rre.setFinishTimestamp(context.getFinishTimestamp());
            rre.setIsSucceed(context.getIsSucceed());
            rre.setExecutionTimespan(context.getExecutionTimespan());
            renRsrecordEntityDAO.saveOrUpdate(rre);
        }
        catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Save resourcing request context but exception occurred, " + ex,
                    TaskContext.class.getName(), LogLevelType.ERROR, context.getRtid());
        }
    }
}
