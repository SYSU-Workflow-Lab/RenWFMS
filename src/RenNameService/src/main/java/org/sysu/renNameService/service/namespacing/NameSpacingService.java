/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.service.namespacing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.entity.RenBoEntity;
import org.sysu.renCommon.entity.RenLogEntity;
import org.sysu.renCommon.entity.RenProcessEntity;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;
import org.sysu.renCommon.interactionRouter.LocationContext;
import org.sysu.renCommon.utility.AuthDomainHelper;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renNameService.GlobalContext;
import org.sysu.renNameService.dao.*;
import org.sysu.renNameService.service.enginescheduler.EngineSchedulerService;
import org.sysu.renNameService.utility.*;

import java.util.*;

/**
 * Author: Rinkako
 * Date  : 2018/1/26
 * Usage : All name space service will be handled in this service module.
 */

@Service
public class NameSpacingService {

    @Autowired
    private RenProcessEntityDAO renProcessEntityDAO;

    @Autowired
    private RenBoEntityDAO renBoEntityDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenLogEntityDAO renLogEntityDAO;

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private EngineSchedulerService engineSchedulerService;

    /**
     * Create a new process.
     *
     * @param renid       creator renid
     * @param processName process unique name for a specific renid
     * @param mainBOName  process entry point BO name
     * @return process pid
     */
    @Transactional(rollbackFor = Exception.class)
    public String CreateProcess(String renid, String processName, String mainBOName) {
        try {
            RenProcessEntity rpe = new RenProcessEntity();
            String pid = "Process_" + UUID.randomUUID().toString();
            rpe.setPid(pid);
            rpe.setCreateTimestamp(TimestampUtil.GetCurrentTimestamp());
            rpe.setCreatorRenid(renid);
            rpe.setMainBo(mainBOName);
            rpe.setProcessName(processName);
            rpe.setAverageCost(0L);
            rpe.setLaunchCount(0);
            rpe.setSuccessCount(0);
            renProcessEntityDAO.saveOrUpdate(rpe);
            return pid;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Create process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return "";
        }
    }

    /**
     * Upload a BO for a specific process.
     *
     * @param pid     belong to pid
     * @param name    BO name
     * @param content BO content string
     * @return pair of boid - involved business role names string
     */
    public AbstractMap.SimpleEntry<String, String> UploadBOContent(String pid, String name, String content) {
        try {
            String boid = "BO_" + UUID.randomUUID().toString();
            boolean cmtFlag = assistantService.newBo(boid, pid, name, content);
            if (!cmtFlag) {
                throw new RuntimeException("Error in creating a new bo");
            }
            // send to engine for get business role
            HashMap<String, String> args = new HashMap<>();
            args.put("boidlist", boid);
            String involveBRs = GlobalContext.Interaction.Send(engineSchedulerService.getRandomBOEngine() + LocationContext.URL_BOENGINE_SERIALIZEBO, args, "");
            return new AbstractMap.SimpleEntry<>(boid, involveBRs);
        } catch (Exception ex) {
            LogUtil.Log("Upload BO but exception occurred, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get all processes of one ren user.
     *
     * @param renid ren user id
     * @return a list of process
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenProcessEntity> GetProcessByRenId(String renid) {
        try {
            return renProcessEntityDAO.getProcessByRenId(renid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get Processes of Ren User but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get all processes of one domain.
     *
     * @param domain domain name
     * @return a list of process
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenProcessEntity> GetProcessByDomain(String domain) {
        boolean cmtFlag = false;
        try {
            List<RenProcessEntity> qRet = renProcessEntityDAO.getProcessByDomain("@" + domain);
            cmtFlag = true;
            List<RenProcessEntity> pureRet = new ArrayList<>();
            for (RenProcessEntity cp : qRet) {
                if (AuthDomainHelper.GetDomainByAuthName(cp.getCreatorRenid()).equals(domain)) {
                    pureRet.add(cp);
                }
            }
            return pureRet;
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Get Processes of domain but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
        }
        return null;
    }

    /**
     * Get all processes by process global id.
     *
     * @param pid process global id
     * @return process instance
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public RenProcessEntity GetProcessByPid(String pid) {
        try {
            return renProcessEntityDAO.findByPid(pid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get Processes of pid but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get the BOs in a process.
     *
     * @param pid process id
     * @return a list of BO in the specific process
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<Object> GetProcessBOList(String pid) {
        try {
            return renBoEntityDAO.findBoIdAndBoNameByPid(pid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get BO in Process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Check if a process name is already existing in a ren user process list.
     *
     * @param renid       ren user id
     * @param processName process name
     * @return boolean for process name existence
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public boolean ContainProcess(String renid, String processName) {
        try {
            List<RenProcessEntity> qRet = renProcessEntityDAO.findRenProcessEntitiesByCreatorRenidAndProcessName(renid, processName);
            return qRet.size() > 0;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get BO in Process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return false;
        }
    }

    /**
     * Get a BO context by its id.
     *
     * @param boid BO unique id
     * @return {@code RenBoEntity} instance
     */
    @Transactional(rollbackFor = Exception.class)
    public RenBoEntity GetBO(String boid, String rtid) {
        try {
            return renBoEntityDAO.findByBoId(boid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get BO context but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return null;
        }
    }

    /**
     * Submit a process launch request.
     *
     * @param pid              process id to be launched
     * @param from             launch platform
     * @param renid            ren user id
     * @param authoritySession service authority session id
     * @param bindingType      resource binding type
     * @param launchType       process launch type
     * @param failureType      process failure catch type
     * @param binding          resource binding source, only useful when static XML binding
     * @return Runtime record package
     */
    @Transactional(rollbackFor = Exception.class)
    public String SubmitProcess(String pid,
                                String from,
                                String renid,
                                String authoritySession,
                                Integer bindingType,
                                Integer launchType,
                                Integer failureType,
                                Integer authType,
                                String binding) {
        try {
            RenProcessEntity rpe = renProcessEntityDAO.findByPid(pid);
            rpe.setAuthtype(authType);
            String authSign = "";
            if (authType != 0) {
                authSign = RSASignatureUtil.Signature(pid, GlobalContext.PRIVATE_KEY);
                assert authSign != null;
                rpe.setSelfsignature(authSign);
            }
            RenRuntimerecordEntity rrte = new RenRuntimerecordEntity();
            String rtid = String.format("RTID_%s_%s", renid, UUID.randomUUID());
            rrte.setRtid(rtid);
            rrte.setLaunchFrom(from);
            rrte.setSessionId(authoritySession);
            rrte.setProcessId(pid);
            rrte.setProcessName(rpe.getProcessName());
            rrte.setResourceBindingType(bindingType);
            rrte.setLaunchType(launchType);
            rrte.setFailureType(failureType);
            rrte.setResourceBinding(binding);
            rrte.setIsSucceed(0);
            renProcessEntityDAO.saveOrUpdate(rpe);
            renRuntimerecordEntityDAO.saveOrUpdate(rrte);
            return String.format("%s,%s", rtid, authSign);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log(String.format("Submit process but exception occurred(pid: %s), service rollback, %s", pid, ex), NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Start a process.
     *
     * @param rtid process rtid.
     */
    public void StartProcess(String rtid) throws Exception {
        try {
            boolean cmtFlag = assistantService.startProcess(rtid);
            if (!cmtFlag) {
                throw new RuntimeException("Error in starting the process");
            }
            // interaction with BO Engine
            HashMap<String, String> args = new HashMap<>();
            args.put("rtid", rtid);
            GlobalContext.Interaction.Send(engineSchedulerService.getRandomBOEngine() + LocationContext.URL_BOENGINE_START, args, rtid);
        } catch (Exception ex) {
            LogUtil.Log("Cannot interaction with BO Engine for RTID: " + rtid, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            throw ex;
        }
    }

    /**
     * Check a process runtime finish status.
     *
     * @param rtid process rtid.
     * @return a map of status description in JSON
     */
    @Transactional(rollbackFor = Exception.class)
    public String CheckFinish(String rtid) {
        RenRuntimerecordEntity rrte;
        try {
            rrte = renRuntimerecordEntityDAO.findByRtid(rtid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("CheckFinish but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return null;
        }
        if (rrte == null) {
            return null;
        }
        HashMap<String, String> retMap = new HashMap<>();
        boolean isFinished = rrte.getFinishTimestamp() != null;
        retMap.put("IsFinished", isFinished ? "true" : "false");
        retMap.put("FinishTimestamp", isFinished ? rrte.getFinishTimestamp().toString() : "");
        retMap.put("IsSucceed", rrte.getIsSucceed() == 1 ? "true" : "false");
        return SerializationUtil.JsonSerialization(retMap, rtid);
    }

    /**
     * Get a runtime record.
     *
     * @param rtid runtime record id
     * @return RTC instance
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public RenRuntimerecordEntity GetRuntimeRecord(String rtid) {
        try {
            return renRuntimerecordEntityDAO.findByRtid(rtid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get RTC but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return null;
        }
    }

    /**
     * Get all runtime record.
     *
     * @param activeOnly whether only get running record
     * @return a list of RTC
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenRuntimerecordEntity> GetAllRuntimeRecord(String activeOnly) {
        try {
            List<RenRuntimerecordEntity> qRet;
            if (activeOnly.equalsIgnoreCase("true")) {
                qRet = renRuntimerecordEntityDAO.findRenRuntimerecordEntitiesByIsSucceed(0);
            } else {
                qRet = renRuntimerecordEntityDAO.findAll();
            }
            return qRet;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get RTC but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get all runtime record in a domain.
     *
     * @param domain     domain name
     * @param activeOnly whether only get running record
     * @return a list of RTC
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenRuntimerecordEntity> GetRuntimeRecordByDomain(String domain, String activeOnly) {
        boolean cmtFlag = false;
        try {
            List<RenRuntimerecordEntity> qRet;
            if (activeOnly.equalsIgnoreCase("true")) {
                qRet = renRuntimerecordEntityDAO.findRenRuntimerecordEntitiesByIsSucceedAndDomain(0, "@" + domain);
            } else {
                qRet = renRuntimerecordEntityDAO.findRenRuntimerecordEntitiesByDomain("@" + domain);
            }
            cmtFlag = true;
            List<RenRuntimerecordEntity> pureRet = new ArrayList<>();
            for (RenRuntimerecordEntity rre : qRet) {
                if (AuthDomainHelper.GetDomainByAuthName(rre.getLaunchAuthorityId()).equals(domain)) {
                    pureRet.add(rre);
                }
            }
            return pureRet;
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            LogUtil.Log("Get RTC but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get all runtime record launched by a user.
     *
     * @param launcher   launcher auth name
     * @param activeOnly whether only get running record
     * @return a list of RTC
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenRuntimerecordEntity> GetRuntimeRecordByLauncher(String launcher, String activeOnly) {
        try {
            List<RenRuntimerecordEntity> qRet;
            if (activeOnly.equalsIgnoreCase("true")) {
                qRet = renRuntimerecordEntityDAO.findRenRuntimerecordEntitiesByIsSucceedAndLaunchAuthorityId(0, launcher);
            } else {
                qRet = renRuntimerecordEntityDAO.findRenRuntimerecordEntitiesByLaunchAuthorityId(launcher);
            }
            return qRet;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get RTC but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return null;
        }
    }

    /**
     * Get all runtime record log for rtid.
     *
     * @param rtid process runtime record id
     * @return a list of RTC
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<RenLogEntity> GetRuntimeLog(String rtid) {
        try {
            return renLogEntityDAO.findRenLogEntitiesByRtid(rtid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Get RTC Log but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return null;
        }
    }

    /**
     * Handle transshipment of get span tree descriptor.
     *
     * @param rtid process runtime record id
     */
    public Object TransshipGetSpanTree(String rtid) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("rtid", rtid);
        String ret = GlobalContext.Interaction.Send(engineSchedulerService.getBOEngineLocationByRtid(rtid) + LocationContext.URL_BOENGINE_SPANTREE, argMap, rtid);
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of callback event.
     *
     * @param args argument map to sent
     */
    public String TransshipCallback(Map<String, Object> args) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        for (Map.Entry<String, Object> kvp : args.entrySet()) {
            argMap.put(kvp.getKey(), (String) kvp.getValue());
        }
        return GlobalContext.Interaction.Send(engineSchedulerService.getBOEngineLocationByRtid(argMap.get("rtid")) + LocationContext.URL_BOENGINE_CALLBACK, argMap, argMap.get("rtid"));
    }

    /**
     * Handle transshipment of workitem actions.
     *
     * @param action     action name
     * @param workitemId workitem global id
     * @param workerId   worker global id
     * @param payload    payload map in JSON encoded string
     */
    public Object TransshipWorkitem(String action, String workitemId, String workerId, String payload) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("workitemId", workitemId);
        argMap.put("workerId", workerId);
        if (payload != null) {
            argMap.put("payload", payload);
        }
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_WORKITEM + action, argMap, "");
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of workqueue actions.
     *
     * @param action   action name
     * @param rtid     process rtid
     * @param workerId worker global id
     * @param type     workqueue type
     */
    public Object TransshipWorkqueue(String action, String rtid, String workerId, String type) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("rtid", rtid);
        argMap.put("type", type);
        argMap.put("workerId", workerId);
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_QUEUE + action, argMap, rtid);
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of get all workitems.
     *
     * @param rtid process rtid
     */
    public Object TransshipGetAll(String rtid) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("rtid", rtid);
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_WORKITEM + "getAll", argMap, rtid);
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of get all workitems for a domain.
     *
     * @param domain domain name
     */
    public Object TransshipGetAllWorkitemsForDomain(String domain) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("domain", domain);
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_WORKITEM + "getAllForDomain", argMap, "");
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of get all workitems for a participant.
     *
     * @param workerId participant worker global id
     */
    public Object TransshipGetAllActiveForParticipant(String workerId) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("workerId", workerId);
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_WORKITEM + "getAllForParticipant", argMap, "");
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }

    /**
     * Handle transshipment of workitem.
     *
     * @param wid workitem id
     */
    public Object TransshipGetWorkitem(String wid) throws Exception {
        HashMap<String, String> argMap = new HashMap<>();
        argMap.put("wid", wid);
        String ret = GlobalContext.Interaction.Send(LocationContext.GATEWAY_RS_WORKITEM + "get", argMap, "");
        Map retObj = SerializationUtil.JsonDeserialization(ret, Map.class);
        return ((Map) retObj.get("returnElement")).get("data");
    }
}