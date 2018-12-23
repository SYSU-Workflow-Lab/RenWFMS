package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenSessionEntity;
import org.sysu.renNameService.repository.RenSessionEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/11.
 */

@Repository
@CacheConfig(cacheNames = "ns_ren_session")
public class RenSessionEntityDAO {

    @Autowired
    private RenSessionEntityRepository renSessionEntityRepository;

    @CachePut(key = "#p0.token")
    public RenSessionEntity saveOrUpdate(RenSessionEntity renSessionEntity) {
        return renSessionEntityRepository.save(renSessionEntity);
    }

    @Cacheable(key = "#p0")
    public RenSessionEntity findByToken(String token) {
        return renSessionEntityRepository.findOne(token);
    }

    public List<RenSessionEntity> findRenSessionEntitiesByUsernameAndDestroyTimestampIsNotNull(String username) {
        return renSessionEntityRepository.findRenSessionEntitiesByUsernameAndDestroyTimestampIsNotNull(username);
    }

}
