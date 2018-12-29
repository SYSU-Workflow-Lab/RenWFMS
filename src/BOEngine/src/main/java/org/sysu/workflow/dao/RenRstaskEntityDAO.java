package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenRstaskEntity;
import org.sysu.workflow.repository.RenRstaskEntityRepository;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
@CacheConfig(cacheNames = "ren_rstask")
public class RenRstaskEntityDAO {

    @Autowired
    private RenRstaskEntityRepository renRstaskEntityRepository;

    @Cacheable(key = "#p0")
    public RenRstaskEntity findByTaskid(String taskid) {
        return renRstaskEntityRepository.findOne(taskid);
    }

    @CachePut(key = "#p0.taskid")
    public RenRstaskEntity saveOrUpdate(RenRstaskEntity renRstaskEntity) {
        return renRstaskEntityRepository.saveAndFlush(renRstaskEntity);
    }

    public RenRstaskEntity findByBoidAndAndPolymorphismName(String boid, String polymorphismName) {
        return renRstaskEntityRepository.findByBoidAndAndPolymorphismName(boid, polymorphismName);
    }

}
