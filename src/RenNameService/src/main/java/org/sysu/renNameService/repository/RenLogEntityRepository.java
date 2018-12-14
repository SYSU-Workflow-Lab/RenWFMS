package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renNameService.entity.RenLogEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/13.
 */
public interface RenLogEntityRepository extends JpaRepository<RenLogEntity, String> {

    List<RenLogEntity> findRenLogEntitiesByRtid(String rtid);

}
