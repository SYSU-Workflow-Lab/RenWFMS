package org.sysu.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.workflow.dao.RenServiceInfoDAO;

/**
 * Created by Skye on 2019/1/5.
 */

@Service
public class AssistantService {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    public String getRSLocation(){
        return renServiceInfoDAO.findRSLocation();
    }

}
