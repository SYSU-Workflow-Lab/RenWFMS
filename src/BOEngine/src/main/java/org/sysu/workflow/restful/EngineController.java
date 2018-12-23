/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.workflow.service.InteractionService;
import org.sysu.workflow.service.RuntimeManagementService;
import org.sysu.workflow.service.SteadyStepService;
import org.sysu.workflow.utility.LogUtil;
import org.sysu.workflow.utility.SerializationUtil;
import org.sysu.renCommon.dto.ReturnModel;
import org.sysu.renCommon.dto.StatusCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Rinkako
 * Date  : 2018/1/20
 * Usage : Handle requests passed to engine, like process launching or delegation.
 */
@RestController
@RequestMapping("/gateway")
public class EngineController {

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private RuntimeManagementService runtimeManagementService;

    @Autowired
    private SteadyStepService steadyStepService;

    /**
     * launch a process by the rtid
     * @param rtid the runtime record of a process
     * @return response package
     */
    @RequestMapping(value = "/launchProcess", produces = {"application/json"})
    @ResponseBody
    public ReturnModel LaunchProcess(@RequestParam(value = "rtid", required = false) String rtid) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            ArrayList<String> missingParams = new ArrayList<String>();
            if (rtid == null) missingParams.add("rtid");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            runtimeManagementService.LaunchProcess(rtid);
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, "OK");
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

    /**
     * Serialized pre-stored BO XML text and return the involved BO list.
     * @param boidlist BOs to be serialized, separated by `,`
     * @return response package
     */
    @RequestMapping(value = "/serializeBO", produces = {"application/json"})
    @ResponseBody
    public ReturnModel SerializeBO(@RequestParam(value = "boidlist", required = false) String boidlist) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            ArrayList<String> missingParams = new ArrayList<String>();
            if (boidlist == null) missingParams.add("boidlist");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            String jsonify = SerializationUtil.JsonSerialization(runtimeManagementService.SerializeBO(boidlist), "");
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, jsonify);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

    /**
     * Get a user-friendly descriptor of an instance tree.
     * @param rtid process rtid
     * @return response package
     */
    @RequestMapping(value = "/getSpanTree", produces = {"application/json"})
    @ResponseBody
    public ReturnModel GetSpanTreeByRTID(@RequestParam(value = "rtid", required = false) String rtid) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            ArrayList<String> missingParams = new ArrayList<String>();
            if (rtid == null) missingParams.add("rtid");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            String jsonify = runtimeManagementService.GetSpanTreeDescriptor(rtid);
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, jsonify);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

    /**
     * Resume a running process from entity binlog.
     * @param rtid process rtid
     * @return response package
     */
    @RequestMapping(value = "/resume", produces = {"application/json"})
    @ResponseBody
    public ReturnModel Resume(@RequestParam(value = "rtid", required = false) String rtid) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            ArrayList<String> missingParams = new ArrayList<String>();
            if (rtid == null) missingParams.add("rtid");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            String jsonify = SerializationUtil.JsonSerialization(steadyStepService.ResumeSteady(rtid), rtid);
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, jsonify);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

    /**
     * Resume a running process from entity binlog.
     * @param rtidList process rtid in JSON list
     * @return response package
     */
    @RequestMapping(value = "/resumeMany", produces = {"application/json"})
    @ResponseBody
    public ReturnModel ResumeMany(@RequestParam(value = "rtidList", required = false) String rtidList) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            ArrayList<String> missingParams = new ArrayList<String>();
            if (rtidList == null) missingParams.add("rtidList");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            HashMap<String, List> retMap = new HashMap<>();
            retMap.put("failed", steadyStepService.ResumeSteadyMany(rtidList));
            String jsonify = SerializationUtil.JsonSerialization(retMap, "");
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, jsonify);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

    /**
     * Receive callback event from Name Service.
     * @param rtid process rtid (required)
     * @param bo from which BO (required)
     * @param on which callback scene (required)
     * @param event event send to engine (required)
     * @param payload event send to engine
     * @return response package
     */
    @RequestMapping(value = "/callback", produces = {"application/json"})
    @ResponseBody
    public ReturnModel Callback(@RequestParam(value="rtid", required = false)String rtid,
                                @RequestParam(value="bo", required = false)String bo,
                                @RequestParam(value="on", required = false)String on,
                                @RequestParam(value="id", required = false)String id,
                                @RequestParam(value="event", required = false)String event,
                                @RequestParam(value="payload", required = false)String payload) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            List<String> missingParams = new ArrayList<>();
            if (rtid == null) missingParams.add("rtid");
            if (on == null) missingParams.add("on");
            if (event == null) missingParams.add("event");
            if (bo == null && id == null) missingParams.add("bo");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }
            // logic
            if (bo != null) {
                interactionService.DispatchCallbackByNodeId(rtid, bo, on, event, payload);
                if (id != null) {
                    LogUtil.Log("Received callback with both BO and ID, ID will be ignored.",
                            EngineController.class.getName(), LogLevelType.WARNING, rtid);
                }
            }
            else {
                interactionService.DispatchCallbackByNotifiableId(rtid, id, on, event, payload);
            }
            // return
            ReturnModelHelper.StandardResponse(rnModel, StatusCode.OK, "OK");
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.toString());
        }
        return rnModel;
    }

}
