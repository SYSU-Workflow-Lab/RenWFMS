package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renResourcing.context.steady.RenRuntimerecordEntity;

/**
 * Created by Skye on 2018/12/5.
 */

public interface RenRuntimerecordEntityRepository extends JpaRepository<RenRuntimerecordEntity, String> {
}
