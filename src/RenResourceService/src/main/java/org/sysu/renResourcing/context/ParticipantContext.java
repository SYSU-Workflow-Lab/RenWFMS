/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.context;

import lombok.extern.slf4j.Slf4j;
import org.sysu.renCommon.entity.RenRsparticipantEntity;
import org.sysu.renCommon.enums.AgentReentrantType;
import org.sysu.renCommon.enums.WorkerType;
import org.sysu.renResourcing.dao.RenRsparticipantEntityDAO;
import org.sysu.renResourcing.utility.SpringContextUtil;

import java.io.Serializable;

/**
 * Author: Rinkako
 * Date  : 2018/2/4
 * Usage : Task context is an encapsulation of RenRsparticipant in a
 * convenient way for resourcing service.
 */
@Slf4j
public class ParticipantContext implements Serializable, RCacheablesContext {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Worker global id.
     */
    private String workerId;

    /**
     * User-friendly resource name.
     */
    private String displayName;

    /**
     * Worker type enum.
     */
    private WorkerType workerType;

    /**
     * Agent location, null if Human.
     */
    private String agentLocation;

    /**
     * Agent type enum, only valid when worker type is Agent.
     */
    private AgentReentrantType agentType;

    /**
     * Get a participant context by its global id.
     *
     * @param workerId worker global id
     * @return Participant resourcing context, null if exception occurred or assertion error
     */
    public static ParticipantContext GetContext(String rtid, String workerId) {
        try {
            RenRsparticipantEntityDAO renRsparticipantEntityDAO = (RenRsparticipantEntityDAO) SpringContextUtil.getBean("renRsparticipantEntityDAO");
            RenRsparticipantEntity rre = renRsparticipantEntityDAO.findByWorkerGid(workerId);
            return ParticipantContext.GenerateParticipantContext(rre);
        } catch (Exception ex) {
            log.error("Get context but" + ex + " occurred. [workerId:" + workerId + ", rtid:" + rtid + "]");
            return null;
        }
    }

    /**
     * Get worker global id.
     *
     * @return id string
     */
    public String getWorkerId() {
        return this.workerId;
    }

    /**
     * Get a user-friendly display name.
     *
     * @return name string
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Get the worker resource type.
     *
     * @return WorkerType enum
     */
    public WorkerType getWorkerType() {
        return this.workerType;
    }

    /**
     * Get the agent type, valid only worker type is Agent.
     *
     * @return AgentReentrantType enum
     */
    public AgentReentrantType getAgentType() {
        return this.agentType;
    }

    /**
     * Get Agent location, null if Human.
     *
     * @return agent location string
     */
    public String getAgentLocation() {
        return this.agentLocation;
    }

    /**
     * Generate a participant context by a entity entity.
     *
     * @param rsparticipantEntity RS participant entity
     * @return equivalent participant context.
     */
    private static ParticipantContext GenerateParticipantContext(RenRsparticipantEntity rsparticipantEntity) {
        assert rsparticipantEntity != null;
        ParticipantContext context = new ParticipantContext(rsparticipantEntity.getWorkerid(),
                WorkerType.values()[rsparticipantEntity.getType()]);
        context.displayName = rsparticipantEntity.getDisplayname();
        if (context.workerType == WorkerType.Agent) {
            context.agentType = AgentReentrantType.values()[rsparticipantEntity.getReentrantType()];
            context.agentLocation = rsparticipantEntity.getAgentLocation();
        }
        return context;
    }

    /**
     * Create a new participant context.
     * Private constructor for preventing create context without using `{@code ParticipantContext.GetContext}`.
     *
     * @param workerId worker global id
     * @param type     worker type enum
     */
    private ParticipantContext(String workerId, WorkerType type) {
        this.workerId = workerId;
        this.workerType = type;
    }
}
