package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renNameService.entity.RenRolemapEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/5.
 */
public interface RenRolemapEntityRepository extends JpaRepository<RenRolemapEntity, String> {

    List<RenRolemapEntity> findRenRolemapEntitiesByRtid(String rtid);

    List<RenRolemapEntity> findRenRolemapEntitiesByRtidAndBroleName(String rtid, String broleName);

    List<RenRolemapEntity> findRenRolemapEntitiesByRtidAndMappedGid(String rtid, String mappedGid);
}
