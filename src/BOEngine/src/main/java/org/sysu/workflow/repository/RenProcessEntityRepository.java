package org.sysu.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.workflow.entity.RenProcessEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/12.
 */
public interface RenProcessEntityRepository extends JpaRepository<RenProcessEntity, String> {

    @Query(value = "select * from ren_process where creator_renid = ?1 and state = 0", nativeQuery = true)
    List<RenProcessEntity> findRenProcessEntitiesByCreatorRenidAndStateEquals0(String renid);

    @Query(value = "select * from ren_process where locate(?1, creator_renid) > 0 and state = 0", nativeQuery = true)
    List<RenProcessEntity> findRenProcessEntitiesByDomainAndStateEquals0(String domain);

    List<RenProcessEntity> findRenProcessEntitiesByCreatorRenidAndProcessName(String renid, String processName);
}
