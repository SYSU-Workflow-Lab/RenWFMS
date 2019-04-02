/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow.service;

import org.springframework.stereotype.Service;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.workflow.core.BOXMLExecutionContext;
import org.sysu.workflow.core.BOXMLIOProcessor;
import org.sysu.workflow.core.env.MultiStateMachineDispatcher;
import org.sysu.workflow.core.instanceTree.InstanceManager;
import org.sysu.workflow.core.instanceTree.RInstanceTree;
import org.sysu.workflow.core.instanceTree.RTreeNode;
import org.sysu.workflow.core.model.extend.MessageMode;
import org.sysu.workflow.utility.LogUtil;
import org.sysu.workflow.utility.SerializationUtil;

import java.util.HashMap;

/**
 * Author: Rinkako
 * Date  : 2018/3/1
 * Usage : Methods for handling state-machine interaction request outside BO Engine.
 */
@Service
public class InteractionService {

    /**
     * Handle received callback event and dispatch it to destination BO tree node.
     *
     * @param rtid    process rtid (required)
     * @param bo      from which BO (required)
     * @param on      which callback scene (required)
     * @param event   event send to engine (required)
     * @param payload event send to engine
     */
    public void DispatchCallbackByNodeId(String rtid, String bo, String on, String event, String payload) {
        try {
            HashMap payloadMap = payload != null ? SerializationUtil.JsonDeserialization(payload, HashMap.class) : new HashMap();
            RInstanceTree instanceTree = InstanceManager.GetInstanceTree(rtid);
            if (instanceTree == null) {
                LogUtil.Log(String.format("Dispatch callback(BO:%s | ON:%s | EVT:%s | P:%s ), but tree not exist, ignored.",
                        bo, on, event, payload), InteractionService.class.getName(), LogLevelType.WARNING, rtid);
                return;
            }
            RTreeNode mainBONode = instanceTree.Root;
            if (mainBONode == null) {
                LogUtil.Log(String.format("Dispatch callback(BO:%s | ON:%s | EVT:%s | P:%s ), but main BO not exist, ignored.",
                        bo, on, event, payload), InteractionService.class.getName(), LogLevelType.WARNING, rtid);
                return;
            }
            LogUtil.Log(String.format("Dispatch callback(BO:%s | ON:%s | EVT:%s | P:%s )",
                    bo, on, event, payload), InteractionService.class.getName(), LogLevelType.INFO, rtid);
            MultiStateMachineDispatcher dispatcher = (MultiStateMachineDispatcher) mainBONode.getExect().getEventDispatcher();
            BOXMLExecutionContext ctx = mainBONode.getExect();
            dispatcher.send(ctx.Rtid, ctx.NodeId, "", MessageMode.UNICAST, bo, "",
                    BOXMLIOProcessor.DEFAULT_EVENT_PROCESSOR, event, payloadMap, "", 0);
        } catch (Exception ex) {
            LogUtil.Log(String.format("Dispatch callback(BO:%s | ON:%s | EVT:%s | P:%s ), but exception occurred, %s",
                    bo, on, event, payload, ex), InteractionService.class.getName(), LogLevelType.ERROR, rtid);
            throw ex;  // rethrow to controller
        }
    }

    /**
     * Handle received callback event and dispatch it to destination BO tree node.
     *
     * @param rtid    process rtid (required)
     * @param id      to which notifiable id (required)
     * @param on      which callback scene (required)
     * @param event   event send to engine (required)
     * @param payload event send to engine
     */
    public void DispatchCallbackByNotifiableId(String rtid, String id, String on, String event, String payload) {
        try {
            HashMap payloadMap = payload != null ? SerializationUtil.JsonDeserialization(payload, HashMap.class) : new HashMap();
            RInstanceTree instanceTree = InstanceManager.GetInstanceTree(rtid);
            if (instanceTree == null) {
                LogUtil.Log(String.format("Dispatch callback(Notifiable:%s | ON:%s | EVT:%s | P:%s ), but tree not exist, ignored.",
                        id, on, event, payload), InteractionService.class.getName(), LogLevelType.WARNING, rtid);
                return;
            }
            RTreeNode mainBONode = instanceTree.Root;
            if (mainBONode == null) {
                LogUtil.Log(String.format("Dispatch callback(Notifiable:%s | ON:%s | EVT:%s | P:%s ), but main BO not exist, ignored.",
                        id, on, event, payload), InteractionService.class.getName(), LogLevelType.WARNING, rtid);
                return;
            }
            LogUtil.Log(String.format("Dispatch callback(Notifiable:%s | ON:%s | EVT:%s | P:%s )",
                    id, on, event, payload), InteractionService.class.getName(), LogLevelType.INFO, rtid);
            MultiStateMachineDispatcher dispatcher = (MultiStateMachineDispatcher) mainBONode.getExect().getEventDispatcher();
            BOXMLExecutionContext ctx = mainBONode.getExect();
            dispatcher.send(ctx.Rtid, ctx.NodeId, "", MessageMode.TO_NOTIFIABLE_ID, id, "",
                    BOXMLIOProcessor.DEFAULT_EVENT_PROCESSOR, event, payloadMap, "", 0);
        } catch (Exception ex) {
            LogUtil.Log(String.format("Dispatch callback(Notifiable:%s | ON:%s | EVT:%s | P:%s ), but exception occurred, %s",
                    id, on, event, payload, ex), InteractionService.class.getName(), LogLevelType.ERROR, rtid);
            throw ex;  // rethrow to controller
        }
    }
}
