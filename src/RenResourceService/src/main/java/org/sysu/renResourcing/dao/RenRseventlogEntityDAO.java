package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenRseventlogEntity;
import org.sysu.renResourcing.repository.RenRseventlogEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/21.
 */

@Repository
public class RenRseventlogEntityDAO {

    @Autowired
    private RenRseventlogEntityRepository renRseventlogEntityRepository;

    public RenRseventlogEntity saveOrUpdate(RenRseventlogEntity renRseventlogEntity) {
        return renRseventlogEntityRepository.saveAndFlush(renRseventlogEntity);
    }

    public List<RenRseventlogEntity> findRenRseventlogEntitiesByTaskidAndTwoEvent(String taskId, String beginStatus, String endStatus) {
        return renRseventlogEntityRepository.findRenRseventlogEntitiesByTaskidAndTwoEvent(taskId, beginStatus, endStatus);
    }

    public List<RenRseventlogEntity> findRenRseventlogEntitiesByTaskidAndTwoEventAndWorkerId(String taskId, String beginStatus, String endStatus, String workerId) {
        return renRseventlogEntityRepository.findRenRseventlogEntitiesByTaskidAndTwoEventAndWorkerId(taskId, beginStatus, endStatus, workerId);
    }

}
