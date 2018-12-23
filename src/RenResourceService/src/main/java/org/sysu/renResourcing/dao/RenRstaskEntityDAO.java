package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenRstaskEntity;
import org.sysu.renResourcing.repository.RenRstaskEntityRepository;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
@CacheConfig(cacheNames = "rs_ren_rstask")
public class RenRstaskEntityDAO {

    @Autowired
    private RenRstaskEntityRepository renRstaskEntityRepository;

    @Cacheable(key = "#p0")
    public RenRstaskEntity findByTaskid(String taskid) {
        return renRstaskEntityRepository.findOne(taskid);
    }

    public RenRstaskEntity findByBoidAndAndPolymorphismName(String boid, String polymorphismName) {
        return renRstaskEntityRepository.findByBoidAndAndPolymorphismName(boid, polymorphismName);
    }

}
