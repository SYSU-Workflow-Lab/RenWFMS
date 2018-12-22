package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenRsparticipantEntity;
import org.sysu.renResourcing.repository.RenRsparticipantEntityRepository;

/**
 * Created by Skye on 2018/12/5.
 */

@Repository
@CacheConfig(cacheNames = "ren_rsparticipant")
public class RenRsparticipantEntityDAO {

    @Autowired
    private RenRsparticipantEntityRepository renRsparticipantEntityRepository;

    @Cacheable(key = "#p0")
    public RenRsparticipantEntity findByWorkerGid(String workerGid) {
        return renRsparticipantEntityRepository.findOne(workerGid);
    }

    @CachePut(key = "#p0.workerid")
    public RenRsparticipantEntity saveOrUpdate(RenRsparticipantEntity renRsparticipantEntity) {
        return renRsparticipantEntityRepository.save(renRsparticipantEntity);
    }

    @CacheEvict(key = "#p0.workerid")
    public void delete(RenRsparticipantEntity renRsparticipantEntity) {
        renRsparticipantEntityRepository.delete(renRsparticipantEntity);
    }
}
