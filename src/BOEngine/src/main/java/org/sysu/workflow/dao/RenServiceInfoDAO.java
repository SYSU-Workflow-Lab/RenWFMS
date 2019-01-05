package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.workflow.repository.RenServiceInfoRepository;

/**
 * Created by Skye on 2019/1/2.
 */

@Repository
@CacheConfig(cacheNames = "ren_serviceinfo")
public class RenServiceInfoDAO {

    @Autowired
    private RenServiceInfoRepository renServiceInfoRepository;

    @CachePut(key = "#p0.interpreterId")
    public RenServiceInfo saveOrUpdate(RenServiceInfo renServiceInfo) {
        return renServiceInfoRepository.saveAndFlush(renServiceInfo);
    }

    @CacheEvict(key = "#p0")
    public void deleteByInterpreterId(String interpreterId) {
        renServiceInfoRepository.delete(interpreterId);
    }

    public String findRSLocation() {
        return renServiceInfoRepository.findRSLocation();
    }

}
