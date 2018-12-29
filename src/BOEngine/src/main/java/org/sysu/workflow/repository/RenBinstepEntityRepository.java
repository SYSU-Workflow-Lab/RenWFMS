package org.sysu.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renCommon.entity.RenBinstepEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/23.
 */
public interface RenBinstepEntityRepository extends JpaRepository<RenBinstepEntity, String> {

    void deleteRenBinstepEntitiesByRtid(String rtid);

    List<RenBinstepEntity> findRenBinstepEntitiesByRtid(String rtid);

}
