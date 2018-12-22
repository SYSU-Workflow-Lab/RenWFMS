package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenProcessEntity;
import org.sysu.renNameService.repository.RenProcessEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/12.
 */

@Repository
@CacheConfig(cacheNames = "ns_ren_process")
public class RenProcessEntityDAO {

    @Autowired
    private RenProcessEntityRepository renProcessEntityRepository;

    @CachePut(key = "#p0.pid")
    public RenProcessEntity saveOrUpdate(RenProcessEntity renProcessEntity) {
        return renProcessEntityRepository.saveAndFlush(renProcessEntity);
    }

    @Cacheable(key = "#p0")
    public RenProcessEntity findByPid(String pid) {
        return renProcessEntityRepository.findOne(pid);
    }

    public List<RenProcessEntity> getProcessByRenId(String renid) {
        return renProcessEntityRepository.findRenProcessEntitiesByCreatorRenidAndStateEquals0(renid);
    }

    public List<RenProcessEntity> getProcessByDomain(String domain) {
        return renProcessEntityRepository.findRenProcessEntitiesByDomainAndStateEquals0(domain);
    }

    public List<RenProcessEntity> findRenProcessEntitiesByCreatorRenidAndProcessName(String renid, String processName) {
        return renProcessEntityRepository.findRenProcessEntitiesByCreatorRenidAndProcessName(renid, processName);
    }

}
