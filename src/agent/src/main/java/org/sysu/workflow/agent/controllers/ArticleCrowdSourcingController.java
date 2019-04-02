package org.sysu.workflow.agent.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Skye on 2019/4/2.
 */

@RestController
public class ArticleCrowdSourcingController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/")
    public void onPost(@RequestBody JsonNode data) {
        switch (data.get("TaskName").asText()) {
            case "getBestSchemaTask":
                break;
            case "getBestSolutionTask":
                break;
            case "mergeTask":
                break;
        }
    }

}
