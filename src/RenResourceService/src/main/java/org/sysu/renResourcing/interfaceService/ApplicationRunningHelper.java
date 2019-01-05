package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renResourcing.dao.RenServiceInfoDAO;

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

    @Autowired
    private Environment environment;


    @PostConstruct
    public void postConstruct() {
        try {
            renServiceInfoDAO.saveOrUpdate(new RenServiceInfo(GlobalContext.RESOURCE_SERVICE_GLOBAL_ID, "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + environment.getProperty("server.port")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void preDestroy() {
        renServiceInfoDAO.deleteByInterpreterId(GlobalContext.RESOURCE_SERVICE_GLOBAL_ID);
    }

}
