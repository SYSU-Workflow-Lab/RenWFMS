package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenQueueitemsEntity;
import org.sysu.renResourcing.repository.RenQueueitemsEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
public class RenQueueitemsEntityDAO {

    @Autowired
    private RenQueueitemsEntityRepository renQueueitemsEntityRepository;

    public RenQueueitemsEntity saveOrUpdate(RenQueueitemsEntity renQueueitemsEntity) {
        return renQueueitemsEntityRepository.saveAndFlush(renQueueitemsEntity);
    }

    public List<RenQueueitemsEntity> findRenQueueitemsEntitiesByWorkitemId(String workitemId) {
        return renQueueitemsEntityRepository.findRenQueueitemsEntitiesByWorkitemId(workitemId);
    }

    public List<RenQueueitemsEntity> findRenQueueitemsEntitiesByWorkqueueId(String workqueueId) {
        return renQueueitemsEntityRepository.findRenQueueitemsEntitiesByWorkqueueId(workqueueId);
    }

    public void deleteByWorkitemId(String wid) {
        renQueueitemsEntityRepository.deleteByWorkitemId(wid);
    }

    public void deleteByWorkqueueIdAndWorkitemId(String workqueueId, String wid) {
        renQueueitemsEntityRepository.deleteByWorkqueueIdAndWorkitemId(workqueueId, wid);
    }

    public RenQueueitemsEntity findByWorkqueueIdAndWorkitemId(String workqueueId, String wid) {
        return renQueueitemsEntityRepository.findByWorkqueueIdAndWorkitemId(workqueueId, wid);
    }

}
