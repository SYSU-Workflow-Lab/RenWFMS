/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renCommon.enums.PrivilegeType;
import org.sysu.renCommon.enums.ResourceBindingType;
import org.sysu.renResourcing.context.ParticipantContext;
import org.sysu.renResourcing.context.WorkitemContext;
import org.sysu.renCommon.utility.CommonUtil;
import org.sysu.renResourcing.dao.RenRuntimerecordEntityDAO;
import org.sysu.renResourcing.utility.LogUtil;

import java.util.HashSet;

/**
 * Author: Rinkako
 * Date  : 2018/2/9
 * Usage : Implementation of Interface O of Resource Service.
 * Interface O is responsible for resources managements. In RenWFMS, we defined
 * resources in COrgan, and register involved resources to Participant in Name
 * Service. Therefore we assert when Resource Service need to refer a RESOURCE,
 * it has already been registered in entity memory as a PARTICIPANT. So, this
 * interface just for involved resources information retrieving and participants
 * privileges management.
 */

@Service
public class InterfaceO {

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    /**
     * Get current valid participant context set.
     * Current valid means that current resources set in Name Service according to process COrgan isolation type.
     * NOTICE that participant load and unload is handled in Name Service.
     *
     * @param rtid process rtid
     * @return a Hash set for current valid participant context
     */
    public HashSet<ParticipantContext> GetCurrentValidParticipant(String rtid) {
        HashSet<ParticipantContext> retSet = new HashSet<>();
        try {
            RenRuntimerecordEntity runtimeCtx = renRuntimerecordEntityDAO.findByRtid(rtid);
            String participants = runtimeCtx.getParticipantCache();
            if (CommonUtil.IsNullOrEmpty(participants)) {
                return retSet;
            }
            String[] participantPairItem = participants.split(",");
            for (String workerIdBRolePair : participantPairItem) {
                String[] workerItem = workerIdBRolePair.split(":");
                retSet.add(ParticipantContext.GetContext(rtid, workerItem[0]));
            }
            return retSet;
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Get valid participant context set according to business role in specific process runtime.
     *
     * @param rtid  process rtid
     * @param brole business role name
     * @return a Hash set for current valid participant context of a business role
     */
    public HashSet<ParticipantContext> GetParticipantByBRole(String rtid, String brole) {
        HashSet<ParticipantContext> retSet = new HashSet<>();
        try {
            RenRuntimerecordEntity runtimeCtx = renRuntimerecordEntityDAO.findByRtid(rtid);
            String participants = runtimeCtx.getParticipantCache();
            if (CommonUtil.IsNullOrEmpty(participants)) {
                return retSet;
            }
            String[] participantPairItem = participants.split(",");
            for (String workerIdBRolePair : participantPairItem) {
                String[] workerItem = workerIdBRolePair.split(":");
                if (workerItem[1].equals(brole)) {
                    retSet.add(ParticipantContext.GetContext(rtid, workerItem[0]));
                }
            }
            return retSet;
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Check if a participant has a privilege.
     *
     * @param participant participant context
     * @param workitem    workitem context
     * @param privilege   privilege enum
     * @return true if participant has the privilege
     */
    public boolean CheckPrivilege(ParticipantContext participant, WorkitemContext workitem, PrivilegeType privilege) {
        // todo
        return true;
    }

    /**
     * This method is called when sensed participant in entity is changed.
     *
     * @param rtid process rtid
     * @return is fail-fast when organization data changed
     */
    public boolean SenseParticipantDataChanged(String rtid) {
        LogUtil.Log("Sensed binding resources changed.", InterfaceO.class.getName(), LogLevelType.INFO, rtid);
        RenRuntimerecordEntity rre = renRuntimerecordEntityDAO.findByRtid(rtid);
        return rre.getResourceBindingType() == ResourceBindingType.FastFail.ordinal();
    }
}
