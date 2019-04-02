package org.sysu.renNameService.service.routercheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renNameService.dao.RenRuntimerecordEntityDAO;
import org.sysu.renNameService.dao.RenServiceInfoDAO;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Skye on 2019/1/3.
 */

@Service
public class RouterSchedulerService {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    private int next = 0;

    /**
     * Random Scheduler
     */
    public synchronized String getBOEngineLocationByRandom() {
        List<String> allLocations = renServiceInfoDAO.findAllBOEngineLocation();
        return allLocations.get(new Random().nextInt(allLocations.size()));
    }

    /**
     * Round Robin
     */
    public synchronized String getBOEngineLocationByRR() {
        List<String> allLocations = renServiceInfoDAO.findAllBOEngineLocation();
        int index = next;
        next = (next + 1) % allLocations.size();
        return allLocations.get(index);
    }

    /**
     * Hash
     *
     * @param rtid
     */
    public synchronized String getBOEngineLocationByHash(String rtid) {
        List<String> allLocations = renServiceInfoDAO.findAllBOEngineLocation();
        return allLocations.get(rtid.hashCode() % allLocations.size());
    }

    /**
     * Least Connections
     */
    public synchronized String getBOEngineLocationByLC() {
        List<String> leastConnectionsBOEngineLocation = renServiceInfoDAO.findBOEngineLocationByTomcatConcurrency();
        if (leastConnectionsBOEngineLocation.isEmpty()) {

        }
        return leastConnectionsBOEngineLocation.get(0);
    }

    /**
     * Best Fit Decreasing based on Business
     */
    public synchronized String getBOEngineLocationByBusiness() {
        List<String> leastBusyBOEngineLocation = renServiceInfoDAO.findBOEngineLocationByBusiness(3.2);
        if (leastBusyBOEngineLocation.isEmpty()) {

        }
        return leastBusyBOEngineLocation.get(0);
    }

    public String getBOEngineLocationByRtid(String rtid) {
        String interpreterId = renRuntimerecordEntityDAO.findInterpreterIdByRtid(rtid);
        return renServiceInfoDAO.findByInterpreterId(interpreterId).getLocation();
    }

    public String getRSLocation() {
        return renServiceInfoDAO.findRSLocation();
    }

}
