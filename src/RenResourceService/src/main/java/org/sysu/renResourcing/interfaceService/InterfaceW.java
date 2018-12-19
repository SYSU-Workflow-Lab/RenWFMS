/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renCommon.enums.InitializationByType;
import org.sysu.renCommon.enums.WorkQueueType;
import org.sysu.renCommon.enums.WorkitemResourcingStatusType;
import org.sysu.renResourcing.context.*;
import org.sysu.renCommon.utility.AuthDomainHelper;
import org.sysu.renResourcing.utility.LogUtil;
import org.sysu.renCommon.utility.SerializationUtil;

import java.util.*;

/**
 * Author: Rinkako
 * Date  : 2018/2/21
 * Usage : Implementation of Interface W of Resource Service.
 *         Interface W is responsible for providing services for outside clients.
 *         User sub-systems use this interface for manage work queues.
 *         Usually methods in the interface will return result immediately.
 */

@Service
public class InterfaceW {

    @Autowired
    private InterfaceB interfaceB;

    /**
     * Accept offer a workitem.
     * @param ctx rs context
     * @return true for a successful workitem accept
     */
    public boolean AcceptOffer(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Offered)) {
            LogUtil.Log(String.format("Try to accept workitem(%s) but not at Offered status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Accept offer but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when AcceptOffer");
            }
            return false;
        }
        return interfaceB.AcceptOfferedWorkitem(participant, workitem, payload, InitializationByType.USER_INITIATED);
    }

    /**
     * Deallocate a workitem.
     * @param ctx rs context
     * @return true for a successful workitem deallocate
     */
    public boolean Deallocate(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Allocated)) {
            LogUtil.Log(String.format("Try to deallocate workitem(%s) but not at Allocated status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Deallocate but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Deallocate");
            }
            return false;
        }
        return interfaceB.DeallocateWorkitem(participant, workitem, payload);
    }

    /**
     * Start a workitem.
     * @param ctx rs context
     * @return true for a successful workitem start
     */
    public boolean Start(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Allocated)) {
            LogUtil.Log(String.format("Try to start workitem(%s) but not at Allocated status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Start but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Start");
            }
            return false;
        }
        return interfaceB.StartWorkitem(participant, workitem, payload);
    }

    /**
     * Reallocate a workitem.
     * @param ctx rs context
     * @return true for a successful workitem reallocate
     */
    public boolean Reallocate(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Started)) {
            LogUtil.Log(String.format("Try to reallocate workitem(%s) but not at Started status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Reallocate but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Reallocate");
            }
            return false;
        }
        return interfaceB.ReallocateWorkitem(participant, workitem, payload);
    }

    /**
     * Accept and start a workitem.
     * @param ctx rs context
     * @return true for a successful workitem accept and start
     */
    public boolean AcceptAndStart(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Offered)) {
            LogUtil.Log(String.format("Try to accept and start workitem(%s) but not at Offered status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Accept and start but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when AcceptAndStart");
            }
            return false;
        }
        return interfaceB.AcceptOfferedWorkitem(participant, workitem, payload, InitializationByType.SYSTEM_INITIATED);
    }

    /**
     * Skip a workitem.
     * @param ctx rs context
     * @return true for a successful workitem skip
     */
    public boolean Skip(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Allocated)) {
            LogUtil.Log(String.format("Try to skip workitem(%s) but not at Allocated status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Skip but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Skip");
            }
            return false;
        }
        return interfaceB.SkipWorkitem(participant, workitem, payload);
    }

    /**
     * Suspend a workitem.
     * @param ctx rs context
     * @return true for a successful workitem suspend
     */
    public boolean Suspend(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Started)) {
            LogUtil.Log(String.format("Try to suspend workitem(%s) but not at Started status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Suspend but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Suspend");
            }
            return false;
        }
        return interfaceB.SuspendWorkitem(participant, workitem, payload);
    }

    /**
     * Unsuspend a workitem.
     * @param ctx rs context
     * @return true for a successful workitem unsuspend
     */
    public boolean Unsuspend(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Suspended)) {
            LogUtil.Log(String.format("Try to unsuspend workitem(%s) but not at Suspended status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Unsuspend but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Unsuspend");
            }
            return false;
        }
        return interfaceB.UnsuspendWorkitem(participant, workitem, payload);
    }

    /**
     * Complete a workitem.
     * @param ctx rs context
     * @return true for a successful workitem complete
     */
    public boolean Complete(ResourcingContext ctx) {
        String workitemId = (String) ctx.getArgs().get("workitemId");
        String workerId = (String) ctx.getArgs().get("workerId");
        String payload = (String) ctx.getArgs().get("payload");
        WorkitemContext workitem = WorkitemContext.GetContext(workitemId, ctx.getRtid());
        if (!workitem.IsAtResourcingStatus(WorkitemResourcingStatusType.Started)) {
            LogUtil.Log(String.format("Try to complete workitem(%s) but not at Started status", workitemId),
                    InterfaceW.class.getName(), LogLevelType.ERROR, workitem.getEntity().getRtid());
            return false;
        }
        ParticipantContext participant = ParticipantContext.GetContext(ctx.getRtid(), workerId);
        if (workitem == null) {
            LogUtil.Log("Accept offer but workitem not exist, rstid: " + ctx.getRstid(),
                    InterfaceW.class.getName(), LogLevelType.ERROR, ctx.getRtid());
            return false;
        }
        if (participant == null) {
            if (InterfaceO.SenseParticipantDataChanged(ctx.getRtid())) {
                InterfaceX.HandleFastFail(ctx.getRtid());
            }
            else {
                InterfaceX.FailedRedirectToLauncherDomainPool(workitem, "Participant not exist when Complete");
            }
            return false;
        }
        return interfaceB.CompleteWorkitem(participant, workitem, payload);
    }

    /**
     * Get all workitems in all types of queue of a worker.
     * @param ctx rs context
     * @return a dictionary of (WorkQueueType, ListOfWorkitemDescriptors)
     */
    public Map<String, Set<WorkitemContext>> GetWorkQueues(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        String workerId = (String) ctx.getArgs().get("workerId");
        WorkQueueContainer container = WorkQueueContainer.GetContext(workerId);
        HashMap<String, Set<WorkitemContext>> retMap = new HashMap<>();
        Set<WorkitemContext> allocateSet = container.GetQueuedWorkitem(WorkQueueType.ALLOCATED);
        retMap.put(WorkQueueType.ALLOCATED.name(), new HashSet<>());
        for (WorkitemContext workitem : allocateSet) {
            String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
            if (authDomain.equals(domain)) {
                retMap.get(WorkQueueType.ALLOCATED.name()).add(workitem);
            }
        }
        Set<WorkitemContext> offeredSet = container.GetQueuedWorkitem(WorkQueueType.OFFERED);
        retMap.put(WorkQueueType.OFFERED.name(), new HashSet<>());
        for (WorkitemContext workitem : offeredSet) {
            String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
            if (authDomain.equals(domain)) {
                retMap.get(WorkQueueType.OFFERED.name()).add(workitem);
            }
        }
        Set<WorkitemContext> startedSet = container.GetQueuedWorkitem(WorkQueueType.STARTED);
        retMap.put(WorkQueueType.STARTED.name(), new HashSet<>());
        for (WorkitemContext workitem : startedSet) {
            String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
            if (authDomain.equals(domain)) {
                retMap.get(WorkQueueType.STARTED.name()).add(workitem);
            }
        }
        Set<WorkitemContext> suspendSet = container.GetQueuedWorkitem(WorkQueueType.SUSPENDED);
        retMap.put(WorkQueueType.SUSPENDED.name(), new HashSet<>());
        for (WorkitemContext workitem : suspendSet) {
            String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
            if (authDomain.equals(domain)) {
                retMap.get(WorkQueueType.SUSPENDED.name()).add(workitem);
            }
        }
        return retMap;
    }

    /**
     * Get all workitems in a specific type of queue of a worker.
     * @param ctx rs context
     * @return workitem descriptors string in list
     */
    public Set GetWorkQueue(ResourcingContext ctx) {
        String rtid = (String) ctx.getArgs().get("rtid");
        String workerId = (String) ctx.getArgs().get("workerId");
        String queueTypeName = ((String) ctx.getArgs().get("type"));
        String domain = AuthDomainHelper.GetDomainByRTID(rtid);
        WorkQueueType wqType;
        try {
            wqType = WorkQueueType.valueOf(queueTypeName.toUpperCase());
        }
        catch (Exception ex) {
            LogUtil.Log("Illegal queue type: " + queueTypeName, InterfaceW.class.getName(),
                    LogLevelType.ERROR, rtid);
            throw ex;
        }
        WorkQueueContainer container = WorkQueueContainer.GetContext(workerId);
        Set<WorkitemContext> queueSet = container.GetQueuedWorkitem(wqType);
        HashSet retSet = new HashSet();
        for (WorkitemContext workitem : queueSet) {
            String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
            if (authDomain.equals(domain)) {
                retSet.add(workitem.getEntity());
            }
        }
        return retSet;
    }

    /**
     * Get all workitems in a specific type of queue of a list of workers.
     * @param ctx rs context
     * @return workitem descriptors string in map (workerId, list of workitem descriptor)
     */
    public Map GetWorkQueueList(ResourcingContext ctx) {
        String workerIdList = (String) ctx.getArgs().get("workerIdList");
        String[] workerIds = workerIdList.split(",");
        HashMap<String, HashSet> retMap = new HashMap<>();
        for (String workerId : workerIds) {
            String rtid = (String) ctx.getArgs().get("rtid");
            String queueTypeName = ((String) ctx.getArgs().get("type"));
            String domain = AuthDomainHelper.GetDomainByRTID(rtid);
            WorkQueueType wqType = WorkQueueType.valueOf(queueTypeName.toUpperCase());
            WorkQueueContainer container = WorkQueueContainer.GetContext(workerId);
            HashSet<WorkitemContext> queueSet = (HashSet<WorkitemContext>) container.GetQueuedWorkitem(wqType);
            HashSet retSet = new HashSet();
            for (WorkitemContext workitem : queueSet) {
                String authDomain = AuthDomainHelper.GetDomainByRTID(ctx.getRtid());
                if (authDomain.equals(domain)) {
                    retSet.add(workitem.getEntity());
                }
            }
            retMap.put(workerId, retSet);
        }
        return retMap;
    }

    /**
     * Get all active(means not complete) workitems belong to a RTID in user-friendly package.
     * @param ctx rs context
     * @return List of Map of workitem data to return
     */
    public ArrayList<HashMap<String, String>> GetAllActiveWorkitemsInUserFriendly(ResourcingContext ctx) {
        String rtid = (String) ctx.getArgs().get("rtid");
        ArrayList<WorkitemContext> workitemList = WorkitemContext.GetContextRTID(rtid);
        if (workitemList == null) {
            LogUtil.Log("Cannot get workitem for RTID: " + rtid, InterfaceW.class.getName(),
                    LogLevelType.ERROR, rtid);
            return null;
        }
        return WorkitemContext.GenerateResponseWorkitems(workitemList, true);
    }

    /**
     * Get all workitems belong to a domain in user-friendly package.
     * @param ctx rs context
     * @return List of Map of workitem data to return
     */
    public ArrayList<HashMap<String, String>> GetAllWorkitemsInUserFriendlyForDomain(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        ArrayList<WorkitemContext> workitemList = WorkitemContext.GetContextInDomain(domain);
        if (workitemList == null) {
            LogUtil.Log("Cannot get workitem for Domain: " + domain, InterfaceW.class.getName(),
                    LogLevelType.ERROR, "");
            return null;
        }
        return WorkitemContext.GenerateResponseWorkitems(workitemList, false);
    }

    /**
     * Get all workitems belong to a participant in user-friendly package.
     * @param ctx rs context
     * @return List of Map of workitem data to return
     */
    public ArrayList<HashMap<String, String>> GetAllWorkitemsInUserFriendlyForParticipant(ResourcingContext ctx) {
        String workerId = (String) ctx.getArgs().get("workerId");
        WorkQueueContainer container = WorkQueueContainer.GetContext(workerId);
        Set<WorkitemContext> worklistedCtxList = container.GetWorklistedQueue().GetQueueAsSet();
        ArrayList<WorkitemContext> wList = new ArrayList<>(worklistedCtxList);
        return WorkitemContext.GenerateResponseWorkitems(wList, false);
    }

    /**
     * Get a workitem in user-friendly package.
     * @param ctx rs context
     * @return List of Map of workitem data to return
     */
    public HashMap<String, String> GetWorkitemInFriendly(ResourcingContext ctx) {
        String wid = (String) ctx.getArgs().get("wid");
        WorkitemContext workitem = WorkitemContext.GetContext(wid, "");
        if (workitem == null) {
            LogUtil.Log("Cannot get workitem for Wid: " + wid, InterfaceW.class.getName(),
                    LogLevelType.ERROR, "");
            return null;
        }
        return WorkitemContext.GenerateResponseWorkitem(workitem);
    }

    /**
     * Get all workers with any non-empty work queue in a domain.
     * @param ctx rs context
     * @return worker gid in a list
     */
    public String GetNotEmptyQueueWorkers(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        WorkQueueContainer adminContainer = WorkQueueContainer.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + domain);
        Set<WorkitemContext> worklisted = adminContainer.GetQueue(WorkQueueType.WORKLISTED).GetQueueAsSet();
        HashSet<String> retParticipantIds = new HashSet<>();
        for (WorkitemContext workitem : worklisted) {
            retParticipantIds.add(workitem.getEntity().getWid());
        }
        return SerializationUtil.JsonSerialization(retParticipantIds);
    }

    /**
     * Get all workers with a non-empty offered work queue in a domain.
     * @param ctx rs context
     * @return worker gid in a list
     */
    public String GetNotEmptyOfferedQueueWorkers(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        WorkQueueContainer adminContainer = WorkQueueContainer.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + domain);
        Set<WorkitemContext> worklisted = adminContainer.GetQueue(WorkQueueType.WORKLISTED).GetQueueAsSet();
        HashSet<String> retParticipantIds = new HashSet<>();
        for (WorkitemContext workitem : worklisted) {
            if (workitem.getEntity().getResourceStatus().equals(WorkitemResourcingStatusType.Offered.name())) {
                retParticipantIds.add(workitem.getEntity().getWid());
            }
        }
        return SerializationUtil.JsonSerialization(retParticipantIds);
    }

    /**
     * Get all workers with a non-empty allocated work queue in a domain.
     * @param ctx rs context
     * @return worker gid in a list
     */
    public String GetNotEmptyAllocatedQueueWorkers(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        WorkQueueContainer adminContainer = WorkQueueContainer.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + domain);
        Set<WorkitemContext> worklisted = adminContainer.GetQueue(WorkQueueType.WORKLISTED).GetQueueAsSet();
        HashSet<String> retParticipantIds = new HashSet<>();
        for (WorkitemContext workitem : worklisted) {
            if (workitem.getEntity().getResourceStatus().equals(WorkitemResourcingStatusType.Allocated.name())) {
                retParticipantIds.add(workitem.getEntity().getWid());
            }
        }
        return SerializationUtil.JsonSerialization(retParticipantIds);
    }

    /**
     * Get all workers with a non-empty allocated or allocated work queue in a domain.
     * @param ctx rs context
     * @return worker gid in a list
     */
    public String GetNotEmptyOfferedAllocatedQueueWorkers(ResourcingContext ctx) {
        String domain = (String) ctx.getArgs().get("domain");
        WorkQueueContainer adminContainer = WorkQueueContainer.GetContext(GlobalContext.WORKQUEUE_ADMIN_PREFIX + domain);
        Set<WorkitemContext> worklisted = adminContainer.GetQueue(WorkQueueType.WORKLISTED).GetQueueAsSet();
        HashSet<String> retParticipantIds = new HashSet<>();
        for (WorkitemContext workitem : worklisted) {
            if (workitem.getEntity().getResourceStatus().equals(WorkitemResourcingStatusType.Offered.name()) ||
                workitem.getEntity().getResourceStatus().equals(WorkitemResourcingStatusType.Allocated.name())) {
                retParticipantIds.add(workitem.getEntity().getWid());
            }
        }
        return SerializationUtil.JsonSerialization(retParticipantIds);
    }
}
