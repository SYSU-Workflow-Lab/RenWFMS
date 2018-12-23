/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.context;

import org.sysu.renCommon.enums.WorkitemResourcingStatusType;
import org.sysu.renResourcing.entity.RenWorkitemEntity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Author: Rinkako
 * Date  : 2018/2/4
 * Usage : Workitem context is an encapsulation of RenWorkitemEntity in a
 *         convenient way for resourcing service.
 */
public class WorkitemContext implements Serializable, RCacheablesContext {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Steady entity.
     */
    private RenWorkitemEntity entity;

    /**
     * Argument dictionary.
     */
    private HashMap<String, String> argsDict;

    /**
     * Template task context.
     */
    private TaskContext taskContext;

    /**
     * Create a new workitem context.
     * Private constructor for preventing create context outside.
     */
    public WorkitemContext() { }

    /**
     * Get workitem entity.
     *
     * @return workitem entity object
     */
    public RenWorkitemEntity getEntity() {
        return this.entity;
    }

    public void setEntity(RenWorkitemEntity entity) {
        this.entity = entity;
    }

    /**
     * Get workitem argument dictionary.
     *
     * @return parameter-argument hash map
     */
    public HashMap<String, String> getArgsDict() {
        return this.argsDict;
    }

    public void setArgsDict(HashMap<String, String> argsDict) {
        this.argsDict = argsDict;
    }

    /**
     * Get the template task context of this workitem.
     *
     * @return TaskContext
     */
    public TaskContext getTaskContext() {
        return this.taskContext;
    }

    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    /**
     * Check if this workitem at a specific resourcing status.
     *
     * @param rtype status to be checked
     * @return true if in this resourcing status
     */
    public boolean IsAtResourcingStatus(WorkitemResourcingStatusType rtype) {
        return rtype.name().equalsIgnoreCase(this.entity.getResourceStatus());
    }
}
