package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenBoEntity;
import org.sysu.renResourcing.repository.RenBoEntityRepository;

/**
 * Created by Skye on 2018/12/12.
 */

@Repository
@CacheConfig(cacheNames = "ren_bo")
public class RenBoEntityDAO {

    @Autowired
    private RenBoEntityRepository renBoEntityRepository;

    @Cacheable(key = "#p0")
    public RenBoEntity findByBoId(String boid) {
        return renBoEntityRepository.findOne(boid);
    }

    public RenBoEntity findByPidAndBoName(String pid, String boName) {
        return renBoEntityRepository.findByPidAndBoName(pid, boName);
    }


}
