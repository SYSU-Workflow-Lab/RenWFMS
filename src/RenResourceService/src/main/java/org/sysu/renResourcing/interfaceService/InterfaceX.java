/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.FailedWorkitemStatusType;
import org.sysu.renCommon.enums.FailedWorkitemVisibilityType;
import org.sysu.renCommon.enums.RSEventType;
import org.sysu.renCommon.utility.AuthDomainHelper;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renResourcing.context.WorkitemContext;
import org.sysu.renCommon.entity.RenExitemEntity;
import org.sysu.renResourcing.dao.RenExitemEntityDAO;

/**
 * Author: Rinkako
 * Date  : 2018/2/9
 * Usage : Implementation of Interface X of Resource Service.
 * Interface X is responsible for process exception handling.
 */

@Service
public class InterfaceX {

    @Autowired
    private InterfaceE interfaceE;

    @Autowired
    private RenExitemEntityDAO renExitemEntityDAO;

    /**
     * Signal a workitem is failed, and redirect it to its admin launcher exception workitem pool.
     *
     * @param workitem failed workitem
     * @param reason   failed reason
     */
    public void FailedRedirectToLauncherDomainPool(WorkitemContext workitem, String reason) {
        interfaceE.WriteLog(workitem, "", RSEventType.exception_lifecycle);
        this.RouteFailedWorkitem(workitem, reason, FailedWorkitemVisibilityType.DomainOnly);
    }

    /**
     * Signal a workitem is failed, and redirect it to BO WFMS admin exception workitem pool.
     *
     * @param workitem failed workitem
     * @param reason   failed reason
     */
    public void FailedRedirecToWFMSAdminPool(WorkitemContext workitem, String reason) {
        interfaceE.WriteLog(workitem, "", RSEventType.exception_lifecycle);
        this.RouteFailedWorkitem(workitem, reason, FailedWorkitemVisibilityType.DomainAndWFMSAdmin);
    }

    /**
     * Signal a workitem which principle parsing failed, and redirect it to its admin launcher exception workitem pool.
     *
     * @param workitem failed workitem
     */
    public void PrincipleParseFailedRedirectToDomainPool(WorkitemContext workitem) {
        interfaceE.WriteLog(workitem, "", RSEventType.exception_principle);
        this.RouteFailedWorkitem(workitem, "Principle Parse Failed.", FailedWorkitemVisibilityType.DomainOnly);
    }

    /**
     * Signal that failed workitem is redirected to launcher offered queue.
     *
     * @param workitem failed workitem
     * @param rtid     process rtid
     */
    @Transactional(rollbackFor = Exception.class)
    public void RedirectToUnofferedQueue(WorkitemContext workitem, String rtid) {
        String handler = AuthDomainHelper.GetAuthNameByRTID(rtid);
        try {
            RenExitemEntity ree = renExitemEntityDAO.findByWid(workitem.getEntity().getWid());
            ree.setStatus(FailedWorkitemStatusType.Redo.ordinal());
            ree.setHandlerAuthName(handler);
            ree.setTimestamp(TimestampUtil.GetCurrentTimestamp());
            renExitemEntityDAO.saveOrUpdate(ree);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * Signal that failed workitem is ignored.
     *
     * @param workitem failed workitem
     * @param rtid     process rtid
     */
    @Transactional(rollbackFor = Exception.class)
    public void RedirectToIgnored(WorkitemContext workitem, String rtid) {
        String handler = AuthDomainHelper.GetAuthNameByRTID(rtid);
        try {
            RenExitemEntity ree = renExitemEntityDAO.findByWid(workitem.getEntity().getWid());
            ree.setStatus(FailedWorkitemStatusType.Ignored.ordinal());
            ree.setHandlerAuthName(handler);
            ree.setTimestamp(TimestampUtil.GetCurrentTimestamp());
            renExitemEntityDAO.saveOrUpdate(ree);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * Route a workitem to exception pool.
     *
     * @param workitem failed workitem
     * @param reason   reason of failure
     */
    @Transactional(rollbackFor = Exception.class)
    public void RouteFailedWorkitem(WorkitemContext workitem, String reason, FailedWorkitemVisibilityType visibility) {
        try {
            RenExitemEntity ree = new RenExitemEntity();
            ree.setRtid(workitem.getEntity().getRtid());
            ree.setWorkitemId(workitem.getEntity().getWid());
            ree.setReason(reason);
            ree.setStatus(FailedWorkitemStatusType.Unhandled.ordinal());
            ree.setVisibility(visibility.ordinal());
            ree.setTimestamp(TimestampUtil.GetCurrentTimestamp());
            renExitemEntityDAO.saveOrUpdate(ree);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        this.NotifyException(workitem);
    }

    /**
     * Handle fast fail of a process runtime.
     *
     * @param rtid process rtid
     */
    public void HandleFastFail(String rtid) {
        // todo
    }

    /**
     * Notify the auth user about exception happened by notify its binding hook URL.
     *
     * @param workitem failed workitem
     */
    public void NotifyException(WorkitemContext workitem) {
        String rtid = workitem.getEntity().getRtid();
        String launcher = AuthDomainHelper.GetAuthNameByRTID(rtid);
        // todo here do notification.
    }
}
