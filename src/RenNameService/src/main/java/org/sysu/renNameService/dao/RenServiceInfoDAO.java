package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.renNameService.repository.RenServiceInfoRepository;

import java.util.List;

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

    @Cacheable(key = "#p0")
    public RenServiceInfo findByInterpreterId(String interpreterId) {
        return renServiceInfoRepository.findOne(interpreterId);
    }

    public List<String> findAllBOEngineLocation() {
        return renServiceInfoRepository.findAllBOEngineLocation();
    }

    @CacheEvict(key = "#p0")
    public void deleteByInterpreterId(String interpreterId) {
        renServiceInfoRepository.delete(interpreterId);
    }

    public String findRSLocation() {
        return renServiceInfoRepository.findRSLocation();
    }

    public List<String> findBOEngineLocationByTomcatConcurrency() {
        return renServiceInfoRepository.findBOEngineLocationByTomcatConcurrency();
    }

    public List<String> findBOEngineLocationByBusiness(double threshold) {
        return renServiceInfoRepository.findBOEngineLocationByBusiness(threshold);
    }

}
