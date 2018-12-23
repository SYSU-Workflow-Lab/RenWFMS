package org.sysu.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.workflow.entity.RenRstaskEntity;

/**
 * Created by Skye on 2018/12/22.
 */
public interface RenRstaskEntityRepository extends JpaRepository<RenRstaskEntity, String> {

    RenRstaskEntity findByBoidAndAndPolymorphismName(String boid, String polymorphismName);

}
