package org.sysu.renNameService.service.routercheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renNameService.dao.RenRuntimerecordEntityDAO;
import org.sysu.renNameService.dao.RenServiceInfoDAO;

import java.util.List;
import java.util.Random;

/**
 * Created by Skye on 2019/1/3.
 */

@Service
public class RouterSchedulerService {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    public String getRandomBOEngineLocation() {
        List<String> allLocations = renServiceInfoDAO.findAllBOEngineLocation();
        return allLocations.get(new Random().nextInt(allLocations.size()));
    }

    public String getBOEngineLocationByRtid(String rtid) {
        String interpreterId = renRuntimerecordEntityDAO.findInterpreterIdByRtid(rtid);
        return renServiceInfoDAO.findByInterpreterId(interpreterId).getLocation();
    }

    public String getRSLocation() {
        return renServiceInfoDAO.findRSLocation();
    }

}
