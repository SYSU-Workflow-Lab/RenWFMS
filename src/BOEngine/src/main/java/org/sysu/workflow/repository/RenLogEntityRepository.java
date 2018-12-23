package org.sysu.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.workflow.entity.RenLogEntity;

/**
 * Created by Skye on 2018/12/13.
 */
public interface RenLogEntityRepository extends JpaRepository<RenLogEntity, String> {
}
