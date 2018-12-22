package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenRsrecordEntity;
import org.sysu.renResourcing.repository.RenRsrecordEntityRepository;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
@CacheConfig(cacheNames = "ren_rsrecord")
public class RenRsrecordEntityDAO {

    @Autowired
    private RenRsrecordEntityRepository renRsrecordEntityRepository;

    @CachePut(key = "#p0.rstid")
    public RenRsrecordEntity saveOrUpdate(RenRsrecordEntity renRsrecordEntity) {
        return renRsrecordEntityRepository.saveAndFlush(renRsrecordEntity);
    }

    @Cacheable(key = "#p0")
    public RenRsrecordEntity findByRstid(String rstid) {
        return renRsrecordEntityRepository.findOne(rstid);
    }

}
