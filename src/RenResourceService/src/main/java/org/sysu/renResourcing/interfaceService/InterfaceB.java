/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.utility.AuthDomainHelper;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renCommon.enums.*;
import org.sysu.renResourcing.consistency.ContextLockManager;
import org.sysu.renResourcing.context.*;
import org.sysu.renResourcing.context.contextService.WorkQueueContainerService;
import org.sysu.renResourcing.context.contextService.WorkQueueContextService;
import org.sysu.renResourcing.context.contextService.WorkitemContextService;
import org.sysu.renResourcing.entity.RenProcessEntity;
import org.sysu.renResourcing.entity.RenRsparticipantEntity;
import org.sysu.renResourcing.entity.RenRuntimerecordEntity;
import org.sysu.renResourcing.entity.RenWorkitemEntity;
import org.sysu.renResourcing.dao.RenProcessEntityDAO;
import org.sysu.renResourcing.dao.RenRsparticipantEntityDAO;
import org.sysu.renResourcing.dao.RenRuntimerecordEntityDAO;
import org.sysu.renResourcing.executor.AllocateInteractionExecutor;
import org.sysu.renResourcing.executor.OfferInteractionExecutor;
import org.sysu.renResourcing.plugin.AgentNotifyPlugin;
import org.sysu.renResourcing.plugin.AsyncPluginRunner;
import org.sysu.renResourcing.principle.PrincipleParser;
import org.sysu.renResourcing.principle.RPrinciple;
import org.sysu.renResourcing.utility.LogUtil;
import org.sysu.renCommon.utility.TimestampUtil;

import java.sql.Timestamp;
import java.util.*;

/**
 * Author: Rinkako
 * Date  : 2018/2/9
 * Usage : Implementation of Interface B of Resource Service.
 * Interface B is responsible for control workitems life-cycle, and provide
 * workqueue operations for participants.
 */

@Service
public class InterfaceB {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private InterfaceA interfaceA;

    @Autowired
    private InterfaceE interfaceE;

    @Autowired
    private InterfaceO interfaceO;

    @Autowired
    private InterfaceX interfaceX;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenRsparticipantEntityDAO renRsparticipantEntityDAO;

    @Autowired
    private RenProcessEntityDAO renProcessEntityDAO;

    /**
     * WorkitemContext Handler.
     */
    @Autowired
    private WorkitemContextService workitemContextService;

    /**
     * WorkQueueContext Handler.
     */
    @Autowired
    private WorkQueueContextService workQueueContextService;

    /**
     * WorkQueueContainer Handler.
     */
    @Autowired
    private WorkQueueContainerService workQueueContainerService;

    /**
     * Handle perform submit task.
     *
     * @param ctx rs context
     */
    public void PerformEngineSubmitTask(ResourcingContext ctx) throws Exception {
        LinkedHashMap mapTaskCtx = (LinkedHashMap) ctx.getArgs().get("taskContext");
        String nodeId = (String) ctx.getArgs().get("nodeId");
        TaskContext taskContext = TaskContext.ParseHashMap(mapTaskCtx);

        // use runtime record to get the admin auth name for admin queue identifier
        RenRuntimerecordEntity runtimeRecord = assistantService.IBfindRuntimerecordEntityByRtid(ctx.getRtid());
        String domain = AuthDomainHelper.GetDomainByRTID(runtimeRecord.getRtid());

        // generate workitem
        WorkitemContext workitem = workitemContextService.GenerateContext(taskContext, ctx.getRtid(), (HashMap) ctx.getArgs().get("taskArgumentsVector"), nodeId);

        // parse resourcing principle
        RPrinciple principle = PrincipleParser.Parse(taskContext.getPrinciple());
        if (principle == null) {
            LogUtil.Log(String.format("Cannot parse principle %s", taskContext.getPrinciple()), InterfaceB.class.getName(),
                    LogLevelType.ERROR, ctx.getRtid());
            interfaceX.PrincipleParseFailedRedirectToDomainPool(workitem);
            return;
        }
        // get valid resources
        HashSet<ParticipantContext> validParticipants = interfaceO.GetParticipantByBRole(ctx.getRtid(), taskContext.getBrole());
        if (validParticipants.isEmpty()) {
            LogUtil.Log("A task cannot be allocated to any valid resources, so it will be put into admin unoffered queue.",
                    InterfaceB.class.getName(), LogLevelType.WARNING, ctx.getRtid());
            // move workitem to admin unoffered queue
            WorkQueueContainer adminContainer = workQueueContainerService.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + domain);
            adminContainer.AddToQueue(workitem, WorkQueueType.UNOFFERED);
            return;
        }
        switch (principle.getDistributionType()) {
            case Allocate:
                // create an allocate interaction
                AllocateInteractionExecutor allocateInteraction = new AllocateInteractionExecutor(
                        taskContext.getTaskId(), InitializationByType.SYSTEM_INITIATED);
                // create an allocator for task principle
                allocateInteraction.BindingAllocator(principle, ctx.getRstid(), ctx.getRtid());
                // do allocate to select a participant for handle this workitem
                ParticipantContext chosenOne = allocateInteraction.PerformAllocation(validParticipants, workitem);
                // put workitem to the chosen participant allocated queue
                WorkQueueContainer container = workQueueContainerService.GetContext(chosenOne.getWorkerId());
                container.AddToQueue(workitem, WorkQueueType.ALLOCATED);
                // change workitem status
                workitem.getEntity().setFiringTime(TimestampUtil.GetCurrentTimestamp());
                this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Allocated, null);
                // notify if agent
                if (chosenOne.getWorkerType() == WorkerType.Agent) {
                    AgentNotifyPlugin allocateAnp = new AgentNotifyPlugin();
                    HashMap<String, String> allocateNotifyMap = new HashMap<>(workitemContextService.GenerateResponseWorkitem(workitem));
                    allocateAnp.AddNotification(chosenOne, allocateNotifyMap, ctx.getRtid());
                    AsyncPluginRunner.AsyncRun(allocateAnp);
                }
                break;
            case Offer:
                // create a filter interaction
                OfferInteractionExecutor offerInteraction = new OfferInteractionExecutor(
                        taskContext.getTaskId(), InitializationByType.SYSTEM_INITIATED);
                // create a filter for task principle
                offerInteraction.BindingFilter(principle, ctx.getRstid(), ctx.getRtid());
                // do filter to select a set of participants for this workitem
                Set<ParticipantContext> chosenSet = offerInteraction.PerformOffer(validParticipants, workitem);
                // put workitem to chosen participants offered queue
                AgentNotifyPlugin offerAnp = new AgentNotifyPlugin();
                HashMap<String, String> offerNotifyMap = new HashMap<>(workitemContextService.GenerateResponseWorkitem(workitem));
                for (ParticipantContext oneInSet : chosenSet) {
                    WorkQueueContainer oneInSetContainer = workQueueContainerService.GetContext(oneInSet.getWorkerId());
                    oneInSetContainer.AddToQueue(workitem, WorkQueueType.OFFERED);
                    // notify if agent
                    if (oneInSet.getWorkerType() == WorkerType.Agent) {
                        offerAnp.AddNotification(oneInSet, offerNotifyMap, ctx.getRtid());
                    }
                }
                // change workitem status
                workitem.getEntity().setFiringTime(TimestampUtil.GetCurrentTimestamp());
                this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Offered, null);
                // do notify
                if (offerAnp.Count(ctx.getRtid()) > 0) {
                    AsyncPluginRunner.AsyncRun(offerAnp);
                }
                break;
            case AutoAllocateIfOfferFailed:
                // todo not implementation
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Handle perform submit task.
     *
     * @param ctx rs context
     */
    @Transactional(rollbackFor = Exception.class)
    public void PerformEngineFinishProcess(ResourcingContext ctx) {
        String rtid = (String) ctx.getArgs().get("rtid");
        String successFlag = (String) ctx.getArgs().get("successFlag");
        try {
            RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
            rre.setFinishTimestamp(TimestampUtil.GetCurrentTimestamp());
            rre.setIsSucceed(Integer.parseInt(successFlag));
            renRuntimerecordEntityDAO.saveOrUpdate(rre);
            String participantCache = rre.getParticipantCache();
            String[] participantItem = participantCache.split(",");
            for (String participantGid : participantItem) {
                // Gid is in pattern of "WorkerGlobalId:BRoleName"
                String workerId = participantGid.split(":")[0];
                RenRsparticipantEntity rpe = renRsparticipantEntityDAO.findByWorkerGid(workerId);
                if (rpe != null) {
                    rpe.setReferenceCounter(rpe.getReferenceCounter() - 1);
                    if (rpe.getReferenceCounter() <= 0) {
                        renRsparticipantEntityDAO.delete(rpe);
                    } else {
                        renRsparticipantEntityDAO.saveOrUpdate(rpe);
                    }
                }
            }
            RenProcessEntity processEntity = renProcessEntityDAO.findByPid(rre.getProcessId());
            processEntity.setSuccessCount(processEntity.getSuccessCount() + 1);
            renProcessEntityDAO.saveOrUpdate(processEntity);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("PerformEngineFinishProcess but exception occurred, " + ex, InterfaceB.class.getName(),
                    LogLevelType.ERROR, rtid);
        }
    }

    /**
     * Handle a participant accept a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param initType    initialization type, a flag for engine internal call
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem accept
     */
    public boolean AcceptOfferedWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload, InitializationByType initType) {
        // remove from all queue
        workQueueContextService.RemoveFromAllQueue(workitem);
        // if internal call, means accept and start
        if (initType == InitializationByType.SYSTEM_INITIATED) {
            // write an allocated event without notification
            this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Allocated, payload, false);
            boolean result = this.StartWorkitem(participant, workitem, payload);
            if (!result) {
                interfaceX.FailedRedirectToLauncherDomainPool(workitem, "AcceptOffered by System but failed to start");
                return false;
            }
        }
        // otherwise workitem should be put to allocated queue
        else {
            WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
            container.MoveOfferedToAllocated(workitem);
            this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Allocated, payload);
        }
        // todo notify if agent
        return true;
    }

    /**
     * Handle a participant deallocate a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem deallocate
     */
    public boolean DeallocateWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        if (interfaceO.CheckPrivilege(participant, workitem, PrivilegeType.CAN_DEALLOCATE)) {
            try {
                WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
                container.MoveAllocatedToOffered(workitem);
                this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Offered, payload);
                return true;
            } catch (Exception ex) {
                interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Deallocate but exception occurred: " + ex);
                return false;
            }
        } else {
            LogUtil.Log(String.format("Participant %s(%s) try to deallocate %s, but no privilege.", participant.getDisplayName(), participant.getWorkerId(), workitem.getEntity().getWid()),
                    InterfaceB.class.getName(), LogLevelType.UNAUTHORIZED, workitem.getEntity().getRtid());
            return false;
        }
    }

    /**
     * Handle a participant start a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem start
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean StartWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        try {
            WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
            container.MoveAllocatedToStarted(workitem);
            RenWorkitemEntity rwe = workitem.getEntity();
            rwe.setLatestStartTime(TimestampUtil.GetCurrentTimestamp());
            rwe.setStartTime(TimestampUtil.GetCurrentTimestamp());
            rwe.setStartedBy(participant.getWorkerId());
            workitemContextService.SaveToSteady(workitem);
            // already started
            if (workitem.getEntity().getStatus().equals(WorkitemStatusType.Executing.name())) {
                this.WorkitemChanged(workitem, WorkitemStatusType.Executing, WorkitemResourcingStatusType.Started, payload);
                return true;
            }
            // start by admin
            if (workitem.getEntity().getResourceStatus().equals(WorkitemResourcingStatusType.Unoffered.name())) {
                RenRuntimerecordEntity runtimeRecord = assistantService.IBfindRuntimerecordEntityByRtid(workitem.getEntity().getRtid());
                // get admin queue for this auth user
                String adminQueuePostfix = runtimeRecord.getSessionId().split("_")[1];
                WorkQueueContainer adminContainer = workQueueContainerService.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + adminQueuePostfix);
                adminContainer.RemoveFromQueue(workitem, WorkQueueType.UNOFFERED);
            }
            this.WorkitemChanged(workitem, WorkitemStatusType.Executing, WorkitemResourcingStatusType.Started, payload);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.Log("ParticipantStart get Runtime record failed. " + ex,
                    InterfaceB.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
    }

    /**
     * Handle a participant reallocate a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem reallocate
     */
    public boolean ReallocateWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        if (interfaceO.CheckPrivilege(participant, workitem, PrivilegeType.CAN_REALLOCATE)) {
            try {
                WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
                container.MoveStartedToAllocated(workitem);
                this.WorkitemChanged(workitem, WorkitemStatusType.Fired, WorkitemResourcingStatusType.Allocated, payload);
                return true;
            } catch (Exception ex) {
                interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Reallocate but exception occurred: " + ex);
                return false;
            }
        } else {
            LogUtil.Log(String.format("Participant %s(%s) try to reallocate %s, but no privilege.", participant.getDisplayName(), participant.getWorkerId(), workitem.getEntity().getWid()),
                    InterfaceB.class.getName(), LogLevelType.UNAUTHORIZED, workitem.getEntity().getRtid());
            return false;
        }
    }

    /**
     * Handle a participant suspend a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem suspend
     */
    public boolean SuspendWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        if (interfaceO.CheckPrivilege(participant, workitem, PrivilegeType.CAN_SUSPEND)) {
            try {
                WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
                container.MoveStartedToSuspend(workitem);
                this.WorkitemChanged(workitem, WorkitemStatusType.Suspended, WorkitemResourcingStatusType.Suspended, payload);
                return true;
            } catch (Exception ex) {
                interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Suspend but exception occurred: " + ex);
                return false;
            }
        } else {
            LogUtil.Log(String.format("Participant %s(%s) try to suspend %s, but no privilege.", participant.getDisplayName(), participant.getWorkerId(), workitem.getEntity().getWid()),
                    InterfaceB.class.getName(), LogLevelType.UNAUTHORIZED, workitem.getEntity().getRtid());
            return false;
        }
    }

    /**
     * Handle a participant unsuspend a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem unsuspend
     */
    public boolean UnsuspendWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        try {
            WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
            container.MoveSuspendToStarted(workitem);
            this.WorkitemChanged(workitem, WorkitemStatusType.Executing, WorkitemResourcingStatusType.Started, payload);
            return true;
        } catch (Exception ex) {
            interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Unsuspend but exception occurred: " + ex);
            return false;
        }
    }

    /**
     * Handle a participant skip a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem skip
     */
    public boolean SkipWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        if (interfaceO.CheckPrivilege(participant, workitem, PrivilegeType.CAN_SKIP)) {
            try {
                WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
                container.RemoveFromQueue(workitem, WorkQueueType.ALLOCATED);
                this.WorkitemChanged(workitem, WorkitemStatusType.ForcedComplete, WorkitemResourcingStatusType.Skipped, payload);
                interfaceE.WriteLog(workitem, participant.getWorkerId(), RSEventType.skip);
                return true;
            } catch (Exception ex) {
                interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Skip but exception occurred: " + ex);
                return false;
            }
        } else {
            LogUtil.Log(String.format("Participant %s(%s) try to skip %s, but no privilege.", participant.getDisplayName(), participant.getWorkerId(), workitem.getEntity().getWid()),
                    InterfaceB.class.getName(), LogLevelType.UNAUTHORIZED, workitem.getEntity().getRtid());
            return false;
        }
    }

    /**
     * Handle a participant complete a workitem.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param payload     payload in JSON encoded string
     * @return true for a successful workitem complete
     */
    public boolean CompleteWorkitem(ParticipantContext participant, WorkitemContext workitem, String payload) {
        try {
            RenWorkitemEntity rwe = workitem.getEntity();
            Timestamp currentTS = TimestampUtil.GetCurrentTimestamp();
            Timestamp startTS = rwe.getStartTime();
            rwe.setExecuteTime(currentTS.getTime() - startTS.getTime());
            rwe.setCompletionTime(currentTS);
            rwe.setCompletedBy(participant.getWorkerId());
            workitemContextService.SaveToSteady(workitem);
            WorkQueueContainer container = workQueueContainerService.GetContext(participant.getWorkerId());
            container.RemoveFromQueue(workitem, WorkQueueType.STARTED);
            this.WorkitemChanged(workitem, WorkitemStatusType.Complete, WorkitemResourcingStatusType.Completed, payload);
            interfaceE.WriteLog(workitem, participant.getWorkerId(), RSEventType.complete);
            return true;
        } catch (Exception ex) {
            interfaceX.FailedRedirectToLauncherDomainPool(workitem, "Complete but exception occurred: " + ex);
            return false;
        }
    }

    /**
     * Change a workitem from one status to another status.
     * NOTICE that while changing workitem status, its belonging work queue do NOT be changed.
     *
     * @param workitem   workitem context
     * @param preStatus  original status
     * @param postStatus destination status
     * @param payload    payload in JSON encoded string
     */
    public void WorkitemStatusChanged(WorkitemContext workitem, WorkitemStatusType preStatus, WorkitemStatusType postStatus, String payload) {
        if (preStatus == postStatus) {
            return;
        }
        this.WorkitemChanged(workitem, postStatus, null, payload);
    }

    /**
     * Change a workitem from one resourcing status to another resourcing status.
     * NOTICE that while changing workitem resourcing status, its belonging work queue do NOT be changed.
     *
     * @param workitem   workitem context
     * @param preStatus  original status
     * @param postStatus destination status
     * @param payload    payload in JSON encoded string
     */
    public void WorkitemResourcingStatusChanged(WorkitemContext workitem, WorkitemResourcingStatusType preStatus, WorkitemResourcingStatusType postStatus, String payload) {
        if (preStatus == postStatus) {
            return;
        }
        this.WorkitemChanged(workitem, null, postStatus, payload);
    }

    /**
     * Change a workitem from one status to another status.
     *
     * @param workitem           workitem context
     * @param toStatus           destination status
     * @param toResourcingStatus destination resourcing status
     * @param payload            payload in JSON encoded string
     */
    private void WorkitemChanged(WorkitemContext workitem, WorkitemStatusType toStatus, WorkitemResourcingStatusType toResourcingStatus, String payload) {
        this.WorkitemChanged(workitem, toStatus, toResourcingStatus, payload, true);
    }

    /**
     * Change a workitem from one status to another status.
     *
     * @param workitem           workitem context
     * @param toStatus           destination status
     * @param toResourcingStatus destination resourcing status
     * @param payload            payload in JSON encoded string
     * @param notify             whether need to process callback and hook
     */
    private void WorkitemChanged(WorkitemContext workitem, WorkitemStatusType toStatus, WorkitemResourcingStatusType toResourcingStatus, String payload, boolean notify) {
        // refresh changed to entity
        ContextLockManager.WriteLock(workitem.getClass(), workitem.getEntity().getWid());
        try {
            if (toStatus != null) {
                workitem.getEntity().setStatus(toStatus.name());
            }
            if (toResourcingStatus != null) {
                workitem.getEntity().setResourceStatus(toResourcingStatus.name());
            }
            workitemContextService.SaveToSteady(workitem);
        } finally {
            ContextLockManager.WriteUnLock(workitem.getClass(), workitem.getEntity().getWid());
        }
        // handle callbacks and hooks
        if (notify) {
            try {
                interfaceA.HandleCallbackAndHook(toStatus, workitem, workitem.getTaskContext(), payload);
            } catch (Exception ex) {
                LogUtil.Log(String.format("Workitem(%s) status changed but failed to handle callbacks and hooks, %s", workitem.getEntity().getWid(), ex),
                        InterfaceB.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
                interfaceX.NotifyException(workitem);
            }
        }
    }
}