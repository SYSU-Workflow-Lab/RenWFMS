package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;
import org.sysu.workflow.repository.RenRuntimerecordEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/5.
 */

@Repository
@CacheConfig(cacheNames = "ren_runtimerecord")
public class RenRuntimerecordEntityDAO {

    @Autowired
    private RenRuntimerecordEntityRepository renRuntimerecordEntityRepository;

    @Cacheable(key = "#p0")
    public RenRuntimerecordEntity findByRtid(String rtid) {
        return renRuntimerecordEntityRepository.findOne(rtid);
    }

    @CachePut(key = "#p0.rtid")
    public RenRuntimerecordEntity saveOrUpdate(RenRuntimerecordEntity renRuntimerecordEntity) {
        return renRuntimerecordEntityRepository.saveAndFlush(renRuntimerecordEntity);
    }

    public List<RenRuntimerecordEntity> findAll() {
        return renRuntimerecordEntityRepository.findAll();
    }

    public List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceed(int isSucceed) {
        return renRuntimerecordEntityRepository.findRenRuntimerecordEntitiesByIsSucceed(isSucceed);
    }

    public List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceedAndDomain(int isSucceed, String domain) {
        return renRuntimerecordEntityRepository.findRenRuntimerecordEntitiesByIsSucceedAndDomain(isSucceed, domain);
    }

    public List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByDomain(String domain) {
        return renRuntimerecordEntityRepository.findRenRuntimerecordEntitiesByDomain(domain);
    }

    public List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceedAndLaunchAuthorityId(int isSucceed, String launcher) {
        return renRuntimerecordEntityRepository.findRenRuntimerecordEntitiesByIsSucceedAndLaunchAuthorityId(isSucceed, launcher);
    }

    public List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByLaunchAuthorityId(String launcher) {
        return renRuntimerecordEntityRepository.findRenRuntimerecordEntitiesByLaunchAuthorityId(launcher);
    }

}
