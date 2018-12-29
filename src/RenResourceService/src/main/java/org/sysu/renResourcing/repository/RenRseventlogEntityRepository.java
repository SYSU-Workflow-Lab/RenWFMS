package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenRseventlogEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/21.
 */
public interface RenRseventlogEntityRepository extends JpaRepository<RenRseventlogEntity, String> {

    @Query(value = "select * from ren_rseventlog where taskid = ?1 and event in (?2, ?3)", nativeQuery = true)
    List<RenRseventlogEntity> findRenRseventlogEntitiesByTaskidAndTwoEvent(String taskId, String beginStatus, String endStatus);

    @Query(value = "select * from ren_rseventlog where taskid = ?1 and event in (?2, ?3) and workerid = ?4", nativeQuery = true)
    List<RenRseventlogEntity> findRenRseventlogEntitiesByTaskidAndTwoEventAndWorkerId(String taskId, String beginStatus, String endStatus, String workerId);

}
