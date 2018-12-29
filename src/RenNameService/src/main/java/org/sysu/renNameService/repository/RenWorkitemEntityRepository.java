package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renCommon.entity.RenWorkitemEntity;

/**
 * Created by Skye on 2018/12/12.
 */
public interface RenWorkitemEntityRepository extends JpaRepository<RenWorkitemEntity, String> {
}
