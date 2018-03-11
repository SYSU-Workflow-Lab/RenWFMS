/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.workflow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sysu.workflow.env.MultiStateMachineDispatcher;
import org.sysu.workflow.env.SimpleErrorReporter;
import org.sysu.workflow.env.jexl.JexlEvaluator;
import org.sysu.workflow.instanceTree.InstanceManager;
import org.sysu.workflow.instanceTree.RInstanceTree;
import org.sysu.workflow.io.SCXMLReader;
import org.sysu.workflow.model.SCXML;
import org.sysu.workflow.model.extend.MessageMode;

import java.net.URL;
import java.util.HashMap;

/**
 * Author: Rinkako
 * Date  : 2018/3/6
 * Usage :
 */
public class CrowdSourcingTest {

    @Before
    public void Prepare() {
        GlobalContext.IsLocalDebug = true;
    }

    @Test
    public void TestCS() throws Exception {
        URL url = SCXMLTestHelper.getResource("Request.xml");
        SCXML scxml = new SCXMLReader().read(url);
        Evaluator evaluator = new JexlEvaluator();
        EventDispatcher dispatcher = new MultiStateMachineDispatcher();
        SCXMLExecutor executor = new SCXMLExecutor(evaluator, dispatcher, new SimpleErrorReporter());
        executor.setStateMachine(scxml);
        executor.setRtid("testRTID");
        executor.go();

        RInstanceTree tree = InstanceManager.GetInstanceTree("testRTID");
        SCXMLExecutionContext ctx = executor.getExctx();

        HashMap<String, Object> submitPayload = new HashMap<>();
        submitPayload.put("taskName", "What?! BO?!");
        submitPayload.put("taskDescription", "Write an article about BO");
        submitPayload.put("judgeCount", 3);
        submitPayload.put("decomposeCount", 3);
        submitPayload.put("decomposeVoteCount", 3);
        submitPayload.put("solveCount", 3);
        submitPayload.put("solveVoteCount", 3);

        dispatcher.send("testRTID", ctx.NodeId, "", MessageMode.TO_NOTIFIABLE_ID, "Request",
                "", SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR, "submit", submitPayload, "", 0);

        Assert.assertTrue(executor.getExctx().getScInstance().getCurrentStatus().isInState("Waiting"));
    }
}
