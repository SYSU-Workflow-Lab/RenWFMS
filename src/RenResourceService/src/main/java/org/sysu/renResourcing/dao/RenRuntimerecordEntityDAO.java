package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenRuntimerecordEntity;
import org.sysu.renResourcing.repository.RenRuntimerecordEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/5.
 */

@Repository
@CacheConfig(cacheNames = "ren_runtimerecord")
public class RenRuntimerecordEntityDAO {

    @Autowired
    private RenRuntimerecordEntityRepository renRuntimerecordEntityRepository;

    @Cacheable(key = "#p0")
    public RenRuntimerecordEntity findByRtid(String rtid) {
        return renRuntimerecordEntityRepository.findOne(rtid);
    }

    @CachePut(key = "#p0.rtid")
    public RenRuntimerecordEntity saveOrUpdate(RenRuntimerecordEntity renRuntimerecordEntity) {
        return renRuntimerecordEntityRepository.saveAndFlush(renRuntimerecordEntity);
    }

    public List<RenRuntimerecordEntity> findAll() {
        return renRuntimerecordEntityRepository.findAll();
    }

}
