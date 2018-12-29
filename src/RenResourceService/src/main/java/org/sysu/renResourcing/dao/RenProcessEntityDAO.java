package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenProcessEntity;
import org.sysu.renResourcing.repository.RenProcessEntityRepository;

/**
 * Created by Skye on 2018/12/12.
 */

@Repository
@CacheConfig(cacheNames = "ren_process")
public class RenProcessEntityDAO {

    @Autowired
    private RenProcessEntityRepository renProcessEntityRepository;

    @CachePut(key = "#p0.pid")
    public RenProcessEntity saveOrUpdate(RenProcessEntity renProcessEntity) {
        return renProcessEntityRepository.saveAndFlush(renProcessEntity);
    }

    @Cacheable(key = "#p0")
    public RenProcessEntity findByPid(String pid) {
        return renProcessEntityRepository.findOne(pid);
    }

}
