package org.sysu.workflow.restful.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sysu.workflow.Context;
import org.sysu.workflow.Evaluator;
import org.sysu.workflow.SCXMLExecutor;
import org.sysu.workflow.env.MulitStateMachineDispatcher;
import org.sysu.workflow.env.SimpleErrorReporter;
import org.sysu.workflow.env.jexl.JexlEvaluator;
import org.sysu.workflow.io.SCXMLReader;
import org.sysu.workflow.model.SCXML;
import org.sysu.workflow.restful.entity.RenBoEntity;
import org.sysu.workflow.restful.entity.RenProcessboEntity;
import org.sysu.workflow.restful.utility.HibernateUtil;
import org.sysu.workflow.restful.utility.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Author: Ariana
 * Date  : 2018/1/22
 * Usage : All xml document parse service will be handled in this service module.
 */
public final class LaunchProcessService {
    /**
     * obtain xml document from database according to the process id and root bo id, and then read it
     * @param pid process id
     * @param roid root BO id
     */
    public static void LaunchProcess(String pid, String roid) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            //根据process id从db中找到该process关联的bo(可能是多个)
            List pbResult = session.createQuery(String.format("FROM RenProcessboEntity WHERE pid = '%s'", pid)).list();
            for (Object pb : pbResult) {
                RenProcessboEntity renProcessboEntity = (RenProcessboEntity) pb;
                String boid = renProcessboEntity.getBoId();
                //根据bo id找到root bo的content
                if(boid.equals(roid)) {
                    List boResult = session.createQuery(String.format("FROM RenBoEntity WHERE boid = '%s'", boid)).list();
                    for (Object bo : boResult) {
                        RenBoEntity boEntity = (RenBoEntity) bo;
                        String boContent = boEntity.getBoContent();
                        //read bo content and then go it
                        LaunchProcessService.executeBO(boContent);
                        break;
                    }
                    break;
                }
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.Log("When read bo content by pid and roid, exception occurred, " + e.toString() + ", service rollback",
                    LaunchProcessService.class.getName(), LogUtil.LogLevelType.ERROR);
            transaction.rollback();
        }
    }

    /**
     * read bo content and go it
     * @param boContent
     */
    public static void executeBO(String boContent){
        InputStream inputStream = new ByteArrayInputStream(boContent.getBytes());
        Evaluator evaluator = new JexlEvaluator();
        SCXMLExecutor executor = new SCXMLExecutor(evaluator, new MulitStateMachineDispatcher(), new SimpleErrorReporter());
        try {
            //解析成SCXML对象
            SCXML scxml = SCXMLReader.read(inputStream);
            //启动状态机实例
           //Evaluator evaluator = EvaluatorFactory.getEvaluator(scxml);
            Context rootContext = evaluator.newContext(null);
            executor.setRootContext(rootContext);
            executor.setStateMachine(scxml);
            executor.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}