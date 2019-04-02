package org.sysu.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.workflow.GlobalContext;
import org.sysu.workflow.dao.RenServiceInfoDAO;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Skye on 2019/1/2.
 */

@Slf4j
@Service
public class ApplicationRunningHelper {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Autowired
    private Environment environment;

    @Autowired
    private RestTemplate restTemplate;

    private String URL;


    @PostConstruct
    public void postConstruct() {
        try {
            URL = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + environment.getProperty("server.port");
            renServiceInfoDAO.saveOrUpdate(new RenServiceInfo(GlobalContext.ENGINE_GLOBAL_ID, URL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void preDestroy() {
        renServiceInfoDAO.deleteByInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
    }

    /**
     * Update engine information per 10 seconds.
     */
    @Scheduled(fixedRate = 10000)
    public void MonitorRunner() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // CPU Load
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(URL + "/jolokia/read/java.lang:type=OperatingSystem/ProcessCpuLoad", String.class);
            String cpuBody = responseEntity.getBody();
            JsonNode cpuRoot = objectMapper.readTree(cpuBody);
            double cpuValue = cpuRoot.get("value").asDouble();
            log.info("当前CPU占用率：" + (cpuValue * 100) + "%");

            // Memory Usage
            responseEntity = restTemplate.getForEntity(URL + "/jolokia/read/java.lang:type=Memory/HeapMemoryUsage", String.class);
            String memoryBody = responseEntity.getBody();
            JsonNode memoryRoot = objectMapper.readTree(memoryBody);
            JsonNode memoryValue = memoryRoot.get("value");
            double memoryResult = memoryValue.get("used").asDouble() / memoryValue.get("committed").asDouble();
            log.info("当前Memory占用率：" + (memoryResult * 100) + "%");

            // Tomcat Threads
            responseEntity = restTemplate.getForEntity(URL + "/jolokia/read/Tomcat:name=\"http-nio-10232\",type=ThreadPool/currentThreadsBusy", String.class);
            String tomcatBody = responseEntity.getBody();
            JsonNode tomcatRoot = objectMapper.readTree(tomcatBody);
            double tomcatValue = tomcatRoot.get("value").asDouble();
            double maxThreads = Double.valueOf(environment.getProperty("server.tomcat.max-threads"));
            double tomcatResult = tomcatValue / maxThreads;
            log.info("当前Tomcat并发数：" + tomcatValue);

            RenServiceInfo serviceInfo = renServiceInfoDAO.findByInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
            serviceInfo.setCpuOccupancyRate(cpuValue);
            serviceInfo.setMemoryOccupancyRate(memoryResult);
            serviceInfo.setTomcatConcurrency(tomcatResult);
            serviceInfo.updateBusiness();
            renServiceInfoDAO.saveOrUpdate(serviceInfo);
        } catch (ResourceAccessException rae) {
            // DO NOTHING
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
