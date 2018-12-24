/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.rolemapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renNameService.GlobalContext;
import org.sysu.renNameService.dao.*;
import org.sysu.renNameService.entity.*;
import org.sysu.renNameService.utility.*;
import org.sysu.renCommon.utility.AuthDomainHelper;

import java.util.*;

/**
 * Author: Rinkako
 * Date  : 2018/1/16
 * Usage : All business role map service will be handled in this service module.
 */

@Service
public class RoleMappingService {

    @Autowired
    private RenRolemapEntityDAO renRolemapEntityDAO;

    @Autowired
    private RenRsparticipantEntityDAO renRsparticipantEntityDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenRolemapArchivedEntityDAO renRolemapArchivedEntityDAO;

    @Autowired
    private RenDomainEntityDAO renDomainEntityDAO;

    /**
     * Get resource description JSON involved in a process.
     *
     * @param rtid process rtid
     * @return String of COrgan response JSON
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public ArrayList<RenRolemapEntity> GetInvolvedResource(String rtid) {
        CachedRoleMap crm = RoleMapCachePool.Retrieve(rtid);
        // from entity
        if (crm == null) {
            try {
                List<RenRolemapEntity> qRet = renRolemapEntityDAO.findRenRolemapEntitiesByRtid(rtid);
                return (ArrayList<RenRolemapEntity>) qRet;
            } catch (Exception ex) {
                LogUtil.Log("When get worker by business role, exception occurred, " + ex.toString() + ", service rollback",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw ex;
            }
        }
        // from cache
        else {
            return crm.getCacheList();
        }
    }

    /**
     * Load participants for a process to be launched soon.
     *
     * @param renid ren auth id
     * @param rtid  process rtid
     * @param nsid  ns
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void LoadParticipant(String renid, String rtid, String nsid) {
        // get involved mappings
        List<RenRolemapEntity> maps = this.GetInvolvedResource(rtid);
        // decompose groups and capabilities into workers
        HashMap<String, HashMap> involvedWorkers = new HashMap<>();
        HashSet<String> pendWorkers = new HashSet<>();
        HashSet<String> pendWorkerGid = new HashSet<>();
        for (RenRolemapEntity rre : maps) {
            String mapped = rre.getMappedGid();
            if (mapped.startsWith("Human_") || mapped.startsWith("Agent_")) {
                pendWorkers.add(mapped + ":" + rre.getBroleName());
                pendWorkerGid.add(mapped);
            } else {
                String jWorker = this.GetWorkerInOrganizableFromCOrgan(renid, rtid, nsid, mapped);
                ArrayList<HashMap<String, String>> workers = SerializationUtil.JsonDeserialization(jWorker, ArrayList.class);
                for (HashMap<String, String> worker : workers) {
                    pendWorkers.add(worker.get("workerId") + ":" + rre.getBroleName());
                    pendWorkerGid.add(worker.get("workerId"));
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String workerPair : pendWorkers) {
            sb.append(workerPair).append(",");
        }
        String workerList = sb.toString();
        if (workerList.length() > 0) {
            workerList = workerList.substring(0, workerList.length() - 1);
        }
        sb = new StringBuilder();
        for (String workerGid : pendWorkerGid) {
            sb.append(workerGid).append(",");
        }
        String sendToCOrgan = sb.toString();
        if (sendToCOrgan.length() > 0) {
            sendToCOrgan = sendToCOrgan.substring(0, sendToCOrgan.length() - 1);
        }

        String wes = this.GetWorkerEntityFromCOrgan(renid, rtid, nsid, sendToCOrgan);
        ArrayList<HashMap<String, String>> weMaps = SerializationUtil.JsonDeserialization(wes, ArrayList.class);
        for (HashMap<String, String> weMap : weMaps) {
            involvedWorkers.put(weMap.get("GlobalId"), weMap);
        }
        // register these workers to participant
        try {
            for (Map.Entry<String, HashMap> mp : involvedWorkers.entrySet()) {
                String workerGid = mp.getKey();
                HashMap workerItem = mp.getValue();
                RenRsparticipantEntity rpe = renRsparticipantEntityDAO.findByWorkerGid(workerGid);
                if (rpe != null) {
                    rpe.setReferenceCounter(rpe.getReferenceCounter() + 1);
                } else {
                    rpe = new RenRsparticipantEntity();
                    rpe.setWorkerid(workerGid);
                    rpe.setReferenceCounter(1);
                    rpe.setNote((String) workerItem.get("Note"));
                    if (workerGid.startsWith("Human_")) {
                        rpe.setDisplayname((String) workerItem.get("PersonId"));
                        rpe.setType(0);
                    } else {
                        rpe.setDisplayname((String) workerItem.get("Name"));
                        rpe.setType(1);
                        rpe.setReentrantType((Integer) workerItem.get("Type"));
                        rpe.setAgentLocation((String) workerItem.get("Location"));
                    }
                }
                renRsparticipantEntityDAO.saveOrUpdate(rpe);
            }
            RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
            rre.setParticipantCache(workerList);
            renRuntimerecordEntityDAO.saveOrUpdate(rre);
        } catch (Exception ex) {
            LogUtil.Log("When load participant, exception occurred, " + ex.toString() + ", service rollback",
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw ex;
        }
    }

    /**
     * Unload participants for a process to be shutdown soon.
     *
     * @param rtid process rtid
     */
    @Transactional(rollbackFor = Exception.class)
    public void UnloadParticipant(String rtid) {
        try {
            RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
            String participantCache = rre.getParticipantCache();
            String[] participantItem = participantCache.split(",");
            for (String participantGid : participantItem) {
                RenRsparticipantEntity rpe = renRsparticipantEntityDAO.findByWorkerGid(participantGid);
                rpe.setReferenceCounter(rpe.getReferenceCounter() - 1);
                if (rpe.getReferenceCounter() < 1) {
                    renRsparticipantEntityDAO.delete(rpe);
                } else {
                    renRsparticipantEntityDAO.saveOrUpdate(rpe);
                }
            }
        } catch (Exception ex) {
            LogUtil.Log("When load participant, exception occurred, " + ex.toString() + ", service rollback",
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw ex;
        }
    }

    /**
     * Get global id of organization workers by business role name.
     *
     * @param rtid      process rtid
     * @param bRoleName business role id name
     * @return ArrayList of Worker global id string
     */
    @Transactional(rollbackFor = Exception.class)
    public ArrayList<String> GetWorkerByBusinessRole(String rtid, String bRoleName) {
        ArrayList<String> retList = new ArrayList<>();
        CachedRoleMap crm = RoleMapCachePool.Retrieve(rtid);
        // from entity
        if (crm == null) {
            crm = new CachedRoleMap();
            try {
                List<RenRolemapEntity> qRet = renRolemapEntityDAO.findRenRolemapEntitiesByRtidAndBroleName(rtid, bRoleName);
                for (RenRolemapEntity rre : qRet) {
                    retList.add(rre.getMappedGid());
                    crm.addCacheItem(rre);
                }
                if (qRet.size() > 0) {
                    crm.setOrganDataVersion(qRet.get(0).getDataVersion());
                }
                RoleMapCachePool.Add(rtid, crm);
            } catch (Exception ex) {
                LogUtil.Log("When get worker by business role, exception occurred, " + ex.toString() + ", service rollback",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw ex;
            }
        }
        // from cache
        else {
            ArrayList<RenRolemapEntity> cachedList = crm.getCacheListByBRole(bRoleName);
            for (RenRolemapEntity rre : cachedList) {
                retList.add(rre.getMappedGid());
            }
        }
        return retList;
    }

    /**
     * Get business role name by a organization context global id.
     *
     * @param rtid     process rtid
     * @param globalId organization context global id
     * @return ArrayList of business role name string
     */
    @Transactional(rollbackFor = Exception.class)
    public ArrayList<String> GetBusinessRoleByGlobalId(String rtid, String globalId) {
        ArrayList<String> retList = new ArrayList<>();
        CachedRoleMap crm = RoleMapCachePool.Retrieve(rtid);
        // from entity
        if (crm == null) {
            crm = new CachedRoleMap();
            try {
                List<RenRolemapEntity> qRet = renRolemapEntityDAO.findRenRolemapEntitiesByRtidAndMappedGid(rtid, globalId);
                for (RenRolemapEntity rre : qRet) {
                    retList.add(rre.getBroleName());
                    crm.addCacheItem(rre);
                }
                if (qRet.size() > 0) {
                    crm.setOrganDataVersion(qRet.get(0).getDataVersion());
                }
                RoleMapCachePool.Add(rtid, crm);
            } catch (Exception ex) {
                LogUtil.Log("When get business role by gid, exception occurred, " + ex.toString() + ", service rollback",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw ex;
            }
        }
        // from cache
        else {
            ArrayList<RenRolemapEntity> cachedList = crm.getCacheListByGid(globalId);
            for (RenRolemapEntity rre : cachedList) {
                retList.add(rre.getBroleName());
            }
        }
        return retList;
    }

    // For BO Engine

    /**
     * Finish role mapping service and dispose cache.
     *
     * @param rtid process rtid
     */
    @Transactional(rollbackFor = Exception.class)
    public void FinishRoleMapService(String rtid) {
        // remove cache
        RoleMapCachePool.Remove(rtid);
        // remove relations in entity memory
        try {
            List<RenRolemapEntity> qRet = renRolemapEntityDAO.findRenRolemapEntitiesByRtid(rtid);
            for (RenRolemapEntity rre : qRet) {
                RenRolemapArchivedEntity rrae = this.AchieveRoleMap(rre);
                renRolemapArchivedEntityDAO.saveOrUpdate(rrae);
                renRolemapEntityDAO.delete(rre);
            }
        } catch (Exception ex) {
            LogUtil.Log("When finish role map service, exception occurred, " + ex.toString() + ", service rollback",
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw ex;
        }
    }

    // For Master Control Panel

    /**
     * Register role map service for a specific process runtime.
     *
     * @param rtid        process rtid
     * @param organGid    organization global id
     * @param dataVersion organization data version
     * @param descriptor  register parameter descriptor string
     */
    @Transactional(rollbackFor = Exception.class)
    public void RegisterRoleMapService(String rtid, String organGid, String dataVersion, String descriptor) {
        ArrayList<AbstractMap.SimpleEntry<String, String>> parsedList = RoleMapParser.Parse(descriptor);
        // create cache map
        CachedRoleMap crm = new CachedRoleMap();
        crm.setOrganDataVersion(dataVersion);
        for (AbstractMap.SimpleEntry<String, String> kvp : parsedList) {
            RenRolemapEntity rre = new RenRolemapEntity();
            String generateUUID = String.format("RoleMap_%s", UUID.randomUUID());
            rre.setMapId(generateUUID);
            rre.setRtid(rtid);
            rre.setBroleName(kvp.getKey());
            rre.setCorganGid(organGid);
            rre.setMappedGid(kvp.getValue());
            rre.setDataVersion(dataVersion);
            crm.addCacheItem(rre);
        }
        // save to entity
        List<RenRolemapEntity> rreList = crm.getCacheList();
        try {
            for (RenRolemapEntity t : rreList) {
                renRolemapEntityDAO.saveOrUpdate(t);
            }
        } catch (Exception ex) {
            LogUtil.Log("When register role map service, exception occurred, " + ex.toString() + ", service rollback",
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw ex;
        }
    }

    /**
     * Get all worker in a group from a Ren auth user COrgan gateway.
     *
     * @param renid     ren auth id
     * @param rtid      process rtid
     * @param nsid      transaction id for signature
     * @param groupName group name
     * @return a list of [human, agent, group, position, capability] json string
     */
    public String GetWorkerInGroupFromCOrgan(String renid, String rtid, String nsid, String groupName) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get resource by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, rtid);
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("groupName", groupName);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getworkeringroup", args, rtid);
        } catch (Exception ex) {
            LogUtil.Log("When GetWorkerInGroupFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return "";
        }
    }

    /**
     * Get worker entity from a Ren auth user COrgan gateway.
     *
     * @param renid ren auth id
     * @param rtid  process rtid
     * @param nsid  transaction id for signature
     * @param gids  organizable global id
     * @return a list of workers json string
     */
    public String GetWorkerEntityFromCOrgan(String renid, String rtid, String nsid, String gids) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get resource by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, rtid);
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("gids", gids);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getworkerentity", args, rtid);
        } catch (Exception ex) {
            LogUtil.Log("When GetWorkerEntityFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return "";
        }
    }

    /**
     * Get all worker in a organizable from a Ren auth user COrgan gateway.
     *
     * @param renid ren auth id
     * @param rtid  process rtid
     * @param nsid  transaction id for signature
     * @param gid   organizable global id
     * @return a list of workers json string
     */
    public String GetWorkerInOrganizableFromCOrgan(String renid, String rtid, String nsid, String gid) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get resource by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, rtid);
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("gid", gid);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getworkerinorganizable", args, rtid);
        } catch (Exception ex) {
            LogUtil.Log("When GetWorkerInPositionFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return "";
        }
    }


    /**
     * Get all resource from a Ren auth user COrgan gateway.
     *
     * @param renid ren auth id
     * @param rtid  process rtid
     * @param nsid  transaction id for signature
     * @return a list of [human, agent, group, position, capability] json string
     */
    public String GetAllResourceFromCOrgan(String renid, String rtid, String nsid) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get resource by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, rtid);
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getresources", args, "");
        } catch (Exception ex) {
            LogUtil.Log("When GetAllResourceFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return "";
        }
    }

    /**
     * Get all relation connections from a Ren auth user COrgan gateway.
     *
     * @param renid ren auth id
     * @param rtid  process rtid
     * @param nsid  transaction id for signature
     * @return a list of connection json string
     */
    public String GetAllConnectionFromCOrgan(String renid, String rtid, String nsid) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get resource by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, rtid);
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getconnections", args, rtid);
        } catch (Exception ex) {
            LogUtil.Log("When GetAllConnectionFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return "";
        }
    }

    /**
     * Get data version from a Ren auth user COrgan gateway.
     *
     * @param renid ren auth id
     * @param nsid  transaction id for signature
     * @return a list of connection json string
     */
    public String GetDataVersionAndGidFromCOrgan(String renid, String nsid) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(AuthDomainHelper.GetDomainByAuthName(renid));
            assert rae != null;
            String corganUrl = rae.getCorganGateway();
            if (corganUrl == null || corganUrl.equals("")) {
                LogUtil.Log("Get data version by COrgan, but auth user does not bind any COrgan gateway",
                        RoleMappingService.class.getName(), LogUtil.LogLevelType.WARNING, "");
                return "";
            }
            HashMap<String, String> args = new HashMap<>();
            String nsSign = RSASignatureUtil.Signature(nsid + "," + renid, GlobalContext.PRIVATE_KEY);
            assert nsSign != null;
            args.put("renid", renid);
            args.put("nsid", nsid);
            args.put("token", RSASignatureUtil.SafeUrlBase64Encode(nsSign));
            return GlobalContext.Interaction.Send(corganUrl + "ns/getdataversiongid", args, "");
        } catch (Exception ex) {
            LogUtil.Log("When GetDataVersionFromCOrgan role map service, exception occurred, " + ex.toString(),
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return "";
        }
    }

    /**
     * Generate an achieve role map context.
     *
     * @param rre role map context
     * @return role map achieved context
     */
    private RenRolemapArchivedEntity AchieveRoleMap(RenRolemapEntity rre) {
        RenRolemapArchivedEntity rrae = new RenRolemapArchivedEntity();
        rrae.setMapId(rre.getMapId());
        rrae.setRtid(rre.getRtid());
        rrae.setBroleName(rre.getBroleName());
        rrae.setCorganGid(rre.getCorganGid());
        rrae.setMappedGid(rre.getMappedGid());
        rrae.setDataVersion(rre.getDataVersion());
        return rrae;
    }
}
