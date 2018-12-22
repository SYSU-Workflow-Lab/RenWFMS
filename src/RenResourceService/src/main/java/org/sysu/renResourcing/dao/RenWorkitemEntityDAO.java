package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenWorkitemEntity;
import org.sysu.renResourcing.repository.RenWorkitemEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
@CacheConfig(cacheNames = "rs_ren_workitem")
public class RenWorkitemEntityDAO {

    @Autowired
    private RenWorkitemEntityRepository renWorkitemEntityRepository;

    @Cacheable(key = "#p0")
    public RenWorkitemEntity findByWid(String wid) {
        return renWorkitemEntityRepository.findOne(wid);
    }

    @CachePut(key = "#p0.wid")
    public RenWorkitemEntity saveOrUpdate(RenWorkitemEntity renWorkitemEntity) {
        return renWorkitemEntityRepository.saveAndFlush(renWorkitemEntity);
    }

    public List<RenWorkitemEntity> findRenWorkitemEntitiesByRtid(String rtid) {
        return renWorkitemEntityRepository.findRenWorkitemEntitiesByRtid(rtid);
    }

    public List<RenWorkitemEntity> findRenWorkitemEntitiesByDomain(String domain) {
        return renWorkitemEntityRepository.findRenWorkitemEntitiesByDomain(domain);
    }

    public List<RenWorkitemEntity> findRenWorkitemEntitiesByFourStatus(String statu1, String statu2, String statu3, String statu4) {
        return renWorkitemEntityRepository.findRenWorkitemEntitiesByFourStatus(statu1, statu2, statu3, statu4);
    }

}
