package org.sysu.renResourcing.interfaceService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.renResourcing.dao.RenRuntimerecordEntityDAO;
import org.sysu.renResourcing.dao.RenServiceInfoDAO;

/**
 * Created by Skye on 2018/12/13.
 */

@Service
@Slf4j
public class AssistantService {

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Autowired
    private ApplicationRunningHelper applicationRunningHelper;

    public String getBOEngineLocationByRtid(String rtid) {
        String interpreterId = renRuntimerecordEntityDAO.findInterpreterIdByRtid(rtid);
        return renServiceInfoDAO.findByInterpreterId(interpreterId).getLocation();
    }

    public void increaseWorkitemCount() {
        applicationRunningHelper.getWorkitemCount().incrementAndGet();
    }

    public void decreaseWorkitemCount() {
        applicationRunningHelper.getWorkitemCount().decrementAndGet();
    }

}
