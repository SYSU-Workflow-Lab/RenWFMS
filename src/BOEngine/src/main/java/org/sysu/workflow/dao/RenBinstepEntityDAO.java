package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.workflow.entity.RenBinstepEntity;
import org.sysu.workflow.repository.RenBinstepEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/23.
 */
@Repository
@CacheConfig(cacheNames = "be_ren_binstep")
public class RenBinstepEntityDAO {

    @Autowired
    private RenBinstepEntityRepository renBinstepEntityRepository;

    @CachePut(key = "#p0.nodeId")
    public RenBinstepEntity saveOrUpdate(RenBinstepEntity renBinstepEntity) {
        return renBinstepEntityRepository.saveAndFlush(renBinstepEntity);
    }

    @Cacheable(key = "#p0")
    public RenBinstepEntity findByNodeId(String nodeId) {
        return renBinstepEntityRepository.findOne(nodeId);
    }

    @CacheEvict(key = "#p0")
    public void deleteRenBinstepEntitiesByRtid(String rtid) {
        renBinstepEntityRepository.deleteRenBinstepEntitiesByRtid(rtid);
    }

    public List<RenBinstepEntity> findRenBinstepEntitiesByRtid(String rtid) {
        return renBinstepEntityRepository.findRenBinstepEntitiesByRtid(rtid);
    }
}
