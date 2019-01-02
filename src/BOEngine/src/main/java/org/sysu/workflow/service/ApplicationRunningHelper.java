package org.sysu.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.workflow.GlobalContext;
import org.sysu.workflow.dao.RenServiceInfoDAO;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;

/**
 * Created by Skye on 2019/1/2.
 */

@Service
public class ApplicationRunningHelper {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @PostConstruct
    public void postConstruct() {
        try {
            renServiceInfoDAO.saveOrUpdate(new RenServiceInfo(GlobalContext.ENGINE_GLOBAL_ID, InetAddress.getLocalHost().getHostAddress()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void preDestroy() {
        renServiceInfoDAO.deleteByInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
    }

}
