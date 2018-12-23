package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenWorkitemEntity;
import org.sysu.renNameService.repository.RenWorkitemEntityRepository;

/**
 * Created by Skye on 2018/12/12.
 */

@Repository
@CacheConfig(cacheNames = "ns_ren_workitem")
public class RenWorkitemEntityDAO {

    @Autowired
    private RenWorkitemEntityRepository renWorkitemEntityRepository;

    @Cacheable(key = "#p0")
    public RenWorkitemEntity findByWid(String wid) {
        return renWorkitemEntityRepository.findOne(wid);
    }

}
