package org.sysu.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenBoEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/12.
 */
public interface RenBoEntityRepository extends JpaRepository<RenBoEntity, String> {

    @Query(value = "select boid, bo_name from ren_bo where pid = ?1", nativeQuery = true)
    List<Object> findBoIdAndBoNameByPid(String pid);

    List<RenBoEntity> findRenBoEntitiesByPid(String pid);

}
