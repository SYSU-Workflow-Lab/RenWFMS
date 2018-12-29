package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/5.
 */

public interface RenRuntimerecordEntityRepository extends JpaRepository<RenRuntimerecordEntity, String> {

    List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceed(int isSucceed);

    @Query(value = "select * from ren_runtimerecord where is_succeed = ?1 and locate(?2, launch_authority_id) > 0", nativeQuery = true)
    List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceedAndDomain(int isSucceed, String domain);

    @Query(value = "select * from ren_runtimerecord where locate(?1, launch_authority_id) > 0", nativeQuery = true)
    List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByDomain(String domain);

    List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByIsSucceedAndLaunchAuthorityId(int isSucceed, String launcher);

    List<RenRuntimerecordEntity> findRenRuntimerecordEntitiesByLaunchAuthorityId(String launcher);
}
