package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenDomainEntity;
import org.sysu.renNameService.repository.RenDomainEntityRepository;

/**
 * Created by Skye on 2018/12/7.
 */

@Repository
@CacheConfig(cacheNames = "ren_domain")
public class RenDomainEntityDAO {

    @Autowired
    private RenDomainEntityRepository renDomainEntityRepository;

    @Cacheable
    public RenDomainEntity findByName(String name) {
        return renDomainEntityRepository.findOne(name);
    }

}
