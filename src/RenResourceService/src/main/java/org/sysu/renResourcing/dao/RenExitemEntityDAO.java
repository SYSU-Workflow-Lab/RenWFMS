package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenExitemEntity;
import org.sysu.renResourcing.repository.RenExitemEntityRepository;

/**
 * Created by Skye on 2018/12/20.
 */

@Repository
@CacheConfig(cacheNames = "ren_exitem")
public class RenExitemEntityDAO {

    @Autowired
    private RenExitemEntityRepository renExitemEntityRepository;

    @CachePut(key = "#p0.workitemId")
    public RenExitemEntity saveOrUpdate(RenExitemEntity renExitemEntity) {
        return renExitemEntityRepository.saveAndFlush(renExitemEntity);
    }

    @Cacheable(key = "#p0")
    public RenExitemEntity findByWid(String wid) {
        return renExitemEntityRepository.findOne(wid);
    }

}
