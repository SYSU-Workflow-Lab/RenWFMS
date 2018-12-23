package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renResourcing.entity.RenWorkitemEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/22.
 */
public interface RenWorkitemEntityRepository extends JpaRepository<RenWorkitemEntity, String> {

    List<RenWorkitemEntity> findRenWorkitemEntitiesByRtid(String rtid);

    @Query(value = "select * from ren_workitem where locate(?1, rtid) > 0", nativeQuery = true)
    List<RenWorkitemEntity> findRenWorkitemEntitiesByDomain(String domain);

    @Query(value = "select * from ren_workitem where resource_status in (?1, ?2, ?3, ?4)", nativeQuery = true)
    List<RenWorkitemEntity> findRenWorkitemEntitiesByFourStatus(String statu1, String statu2, String statu3, String statu4);

}
