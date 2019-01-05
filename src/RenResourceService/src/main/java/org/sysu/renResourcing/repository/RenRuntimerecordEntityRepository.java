package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenRuntimerecordEntity;

/**
 * Created by Skye on 2018/12/5.
 */

public interface RenRuntimerecordEntityRepository extends JpaRepository<RenRuntimerecordEntity, String> {

    @Query(value = "select interpreter_id from ren_runtimerecord where rtid = ?1", nativeQuery = true)
    String findInterpreterIdByRtid(String rtid);

}
