package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.workflow.entity.RenBoEntity;
import org.sysu.workflow.repository.RenBoEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/12.
 */

@Repository
@CacheConfig(cacheNames = "be_ren_bo")
public class RenBoEntityDAO {

    @Autowired
    private RenBoEntityRepository renBoEntityRepository;

    @CachePut(key = "#p0.boid")
    public RenBoEntity saveOrUpdate(RenBoEntity renBoEntity) {
        return renBoEntityRepository.saveAndFlush(renBoEntity);
    }

    @Cacheable(key = "#p0")
    public RenBoEntity findByBoId(String boid) {
        return renBoEntityRepository.findOne(boid);
    }

    public List<Object> findBoIdAndBoNameByPid(String pid) {
        return renBoEntityRepository.findBoIdAndBoNameByPid(pid);
    }

    public List<RenBoEntity> findRenBoEntitiesByPid(String pid) {
        return renBoEntityRepository.findRenBoEntitiesByPid(pid);
    }

}
