package org.sysu.renNameService.service.enginescheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renNameService.dao.RenServiceInfoDAO;

/**
 * Created by Skye on 2019/1/3.
 */

@Service
public class EngineSchedulerService {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

}
