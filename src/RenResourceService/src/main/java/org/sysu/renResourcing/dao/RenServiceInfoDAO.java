package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.renResourcing.repository.RenServiceInfoRepository;

import java.util.List;

/**
 * Created by Skye on 2019/1/2.
 */

@Repository
@CacheConfig(cacheNames = "ren_serviceinfo")
public class RenServiceInfoDAO {

    @Autowired
    private RenServiceInfoRepository renServiceInfoRepository;

    @Cacheable(key = "#p0")
    public RenServiceInfo findByInterpreterId(String interpreterId) {
        return renServiceInfoRepository.findOne(interpreterId);
    }

}
