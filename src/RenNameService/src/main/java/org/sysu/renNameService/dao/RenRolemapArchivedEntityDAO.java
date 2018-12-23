package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenRolemapArchivedEntity;
import org.sysu.renNameService.repository.RenRolemapArchivedEntityRepository;

/**
 * Created by Skye on 2018/12/7.
 */

@Repository
@CacheConfig(cacheNames = "ns_ren_rolemap_archived")
public class RenRolemapArchivedEntityDAO {

    @Autowired
    private RenRolemapArchivedEntityRepository renRolemapArchivedEntityRepository;

    @CachePut(key = "#p0.mapId")
    public RenRolemapArchivedEntity saveOrUpdate(RenRolemapArchivedEntity renRolemapArchivedEntity) {
        return renRolemapArchivedEntityRepository.save(renRolemapArchivedEntity);
    }

}
