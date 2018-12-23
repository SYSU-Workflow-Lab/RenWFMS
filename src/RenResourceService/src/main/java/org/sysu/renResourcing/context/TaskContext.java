/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.context;

import org.sysu.renCommon.enums.WorkitemStatusType;
import org.sysu.renResourcing.entity.RenRstaskEntity;
import org.sysu.renCommon.utility.CommonUtil;
import org.sysu.renCommon.utility.SerializationUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Rinkako
 * Date  : 2018/2/4
 * Usage : Task context is an encapsulation of RenRSTaskEntity in a
 *         convenient way for resourcing service.
 */
public class TaskContext implements Serializable, RCacheablesContext {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Task global id.
     */
    private String taskGlobalId;

    /**
     * Task id, unique in a process.
     */
    private String taskId;

    /**
     * Task name, unique in a process except base BO, for polymorphism.
     */
    private String taskName;

    /**
     * Task resourcing principle.
     */
    private String principle;

    /**
     * Global id of Belong to which BO.
     */
    private String boid;

    /**
     * Global id of Belong to which Process.
     */
    private String pid;

    /**
     * Brole of this task.
     */
    private String brole;

    /**
     * Task documentation.
     */
    private String documentation;

    /**
     * Notification hook dictionary. (Change, NotifyURL)
     */
    private HashMap<String, ArrayList<String>> hooks = new HashMap<>();

    /**
     * Callback event dictionary. (Status, EventName)
     */
    private HashMap<String, ArrayList<String>> callbacks = new HashMap<>();

    /**
     * Parameters vector.
     */
    private ArrayList<String> parameters = new ArrayList<>();

    /**
     * Parse hash map into task context.
     * @param mapObj mapped object
     * @return context object
     */
    @SuppressWarnings("unchecked")
    public static TaskContext ParseHashMap(HashMap mapObj) {
        TaskContext tc = new TaskContext();
        tc.taskGlobalId = (String) mapObj.get("taskGlobalId");
        tc.taskId = (String) mapObj.get("taskId");
        tc.taskName = (String) mapObj.get("taskName");
        tc.principle = (String) mapObj.get("principle");
        tc.brole = (String) mapObj.get("brole");
        tc.boid = (String) mapObj.get("boid");
        tc.pid = (String) mapObj.get("pid");
        tc.documentation = (String) mapObj.get("documentation");
        tc.hooks = (HashMap<String, ArrayList<String>>) mapObj.get("notifyHooks");
        tc.callbacks = (HashMap<String, ArrayList<String>>) mapObj.get("callbackEvents");
        tc.parameters = (ArrayList<String>) mapObj.get("parameters");
        return tc;
    }

    /**
     * Get task global unique id.
     * @return global id, this is NOT defined in BOXML but generated at runtime.
     */
    public String getTaskGlobalId() {
        return this.taskGlobalId;
    }

    /**
     * Get documentation text.
     * @return documentation string
     */
    public String getDocumentation() {
        return this.documentation;
    }

    /**
     * Get the unique id.
     * @return id string
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Get task polymorphism name.
     * @return name string
     */
    public String getTaskName() {
        return this.taskName;
    }

    /**
     * Get the global id of BO which this task belong to.
     * @return BO global id string
     */
    public String getBoid() {
        return this.boid;
    }

    /**
     * Get the global id of Process which this task belong to.
     * @return Process global id string
     */
    public String getPid() {
        return this.pid;
    }

    /**
     * Get the resourcing principle.
     * @return principle string
     */
    public String getPrinciple() {
        return this.principle;
    }

    /**
     * Get the business role name.
     * @return business role name string
     */
    public String getBrole() {
        return this.brole;
    }

    /**
     * Get the notification hooks dictionary.
     * @return HashMap of (ChangedName-NotifyURL)
     */
    public HashMap<String, ArrayList<String>> getNotifyHooks() {
        return this.hooks;
    }

    /**
     * Get the callback events dictionary.
     * @return HashMap of (Status-EventName)
     */
    public HashMap<String, ArrayList<String>> getCallbackEvents() {
        return this.callbacks;
    }

    /**
     * Get callback events by status type.
     * @param statusType status enum
     * @return ArrayList of callback event name
     */
    public ArrayList<String> getCallbackEventsOfStatus(WorkitemStatusType statusType) {
        ArrayList<String> retList = this.callbacks.get(statusType.name().toUpperCase());
        return retList == null ? new ArrayList<>() : retList;
    }

    /**
     * Get callback hooks by status type.
     * @param statusType status enum
     * @return ArrayList of callback event name
     */
    public ArrayList<String> getCallbackHooksOfStatus(WorkitemStatusType statusType) {
        ArrayList<String> retList = this.hooks.get(statusType.name().toUpperCase());
        return retList == null ? new ArrayList<>() : retList;
    }


    /**
     * Get the parameter vector.
     * @return ArrayList of parameter name
     */
    public ArrayList<String> getParameters() {
        return this.parameters;
    }

    /**
     * Parse hooks by a descriptor in entity.
     * @param hookJSONDescriptor JSON descriptor
     */
    @SuppressWarnings("unchecked")
    private void ParseHooks(String hookJSONDescriptor) {
        this.hooks = new HashMap<>();
        HashMap<String, ArrayList<String>> deMap = SerializationUtil.JsonDeserialization(hookJSONDescriptor, HashMap.class);
        for (Map.Entry<String, ArrayList<String>> kvp : deMap.entrySet()) {
            this.hooks.put(kvp.getKey().toUpperCase(), kvp.getValue());
        }
    }

    /**
     * Parse callback events by a descriptor in entity.
     * @param callbackJSONDescriptor JSON descriptor
     */
    @SuppressWarnings("unchecked")
    private void ParseCallbacks(String callbackJSONDescriptor) {
        this.callbacks = new HashMap<>();
        HashMap<String, ArrayList<String>> deMap = SerializationUtil.JsonDeserialization(callbackJSONDescriptor, HashMap.class);
        for (Map.Entry<String, ArrayList<String>> kvp : deMap.entrySet()) {
            this.callbacks.put(kvp.getKey().toUpperCase(), kvp.getValue());
        }
    }

    /**
     * Parse parameter vector by a descriptor in entity.
     * @param parametersDescriptor parameter string descriptor
     */
    @SuppressWarnings("unchecked")
    private void ParseParameters(String parametersDescriptor) {
        HashMap<String, String> paras = SerializationUtil.JsonDeserialization(parametersDescriptor, HashMap.class);
        this.parameters = new ArrayList<>();
        this.parameters.addAll(paras.keySet());
    }

    /**
     * Generate a task context by a entity entity.
     * @param rstaskEntity RS task entity
     * @param pid Belong to process global id
     * @return equivalent task context.
     */
    public static TaskContext GenerateTaskContext(RenRstaskEntity rstaskEntity, String pid) {
        assert rstaskEntity != null;
        TaskContext context = new TaskContext(rstaskEntity.getPolymorphismId(), rstaskEntity.getPolymorphismName(),
                rstaskEntity.getBrole(), pid, rstaskEntity.getBoid(),
                rstaskEntity.getPrinciple(), rstaskEntity.getDocumentation());
        String hookDescriptor = rstaskEntity.getHookdescriptor();
        if (!CommonUtil.IsNullOrEmpty(hookDescriptor)) {
            context.ParseHooks(hookDescriptor);
        }
        String eventDescriptor = rstaskEntity.getEventdescriptor();
        if (!CommonUtil.IsNullOrEmpty(eventDescriptor)) {
            context.ParseCallbacks(eventDescriptor);
        }
        String parametersDescriptor = rstaskEntity.getParameters();
        if (!CommonUtil.IsNullOrEmpty(parametersDescriptor)) {
            context.ParseParameters(parametersDescriptor);
        }
        context.taskGlobalId = rstaskEntity.getTaskid();
        return context;
    }

    /**
     * Create a new context.
     * Private constructor for preventing create context without using `{@code TaskContext.GetContext}`.
     * @param id task unique id
     * @param name task name
     * @param brole business role name
     * @param pid belong to Process global id
     * @param boid belong to BO global id
     * @param principle resourcing principle
     * @param documentation task documentation text
     */
    private TaskContext(String id, String name, String brole, String pid, String boid, String principle, String documentation) {
        this.taskId = id;
        this.taskName = name;
        this.brole = brole;
        this.pid = pid;
        this.boid = boid;
        this.principle = principle;
        this.documentation = documentation;
    }

    /**
     * Create a new context.
     * Private constructor for preventing create context without using `{@code TaskContext.GetContext}`.
     */
    private TaskContext() { }
}
