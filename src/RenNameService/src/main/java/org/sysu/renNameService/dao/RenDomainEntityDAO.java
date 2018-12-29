package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenDomainEntity;
import org.sysu.renNameService.repository.RenDomainEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/7.
 */

@Repository
@CacheConfig(cacheNames = "ren_domain")
public class RenDomainEntityDAO {

    @Autowired
    private RenDomainEntityRepository renDomainEntityRepository;

    @Cacheable(key = "#p0")
    public RenDomainEntity findByName(String name) {
        return renDomainEntityRepository.findOne(name);
    }

    public RenDomainEntity findByUrlsafeSignature(String signature) {
        return renDomainEntityRepository.findByUrlsafeSignature(signature);
    }

    public List<RenDomainEntity> findAll() {
        return renDomainEntityRepository.findAll();
    }

    @CachePut(key = "#p0.name")
    public RenDomainEntity saveOrUpdate(RenDomainEntity renDomainEntity) {
        return renDomainEntityRepository.save(renDomainEntity);
    }

}
