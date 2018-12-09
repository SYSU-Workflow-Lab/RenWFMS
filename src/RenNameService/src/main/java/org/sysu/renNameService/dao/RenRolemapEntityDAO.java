package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenRolemapEntity;
import org.sysu.renNameService.repository.RenRolemapEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/5.
 */

@Repository
@CacheConfig(cacheNames = "ren_rolemap")
public class RenRolemapEntityDAO {

    @Autowired
    private RenRolemapEntityRepository renRolemapEntityRepository;

    @CachePut(key = "#p0.mapId")
    public RenRolemapEntity saveOrUpdate(RenRolemapEntity renRolemapEntity) {
        return renRolemapEntityRepository.save(renRolemapEntity);
    }

    @CacheEvict(key = "#p0.mapId")
    public void delete(RenRolemapEntity renRolemapEntity) {
        renRolemapEntityRepository.delete(renRolemapEntity);
    }

    public List<RenRolemapEntity> findRenRolemapEntitiesByRtid(String rtid) {
        return renRolemapEntityRepository.findRenRolemapEntitiesByRtid(rtid);
    }

    public List<RenRolemapEntity> findRenRolemapEntitiesByRtidAndBroleName(String rtid, String broleName) {
        return renRolemapEntityRepository.findRenRolemapEntitiesByRtidAndBroleName(rtid, broleName);
    }

    public List<RenRolemapEntity> findRenRolemapEntitiesByRtidAndMappedGid(String rtid, String mappedGid) {
        return renRolemapEntityRepository.findRenRolemapEntitiesByRtidAndMappedGid(rtid, mappedGid);
    }
}
