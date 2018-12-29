package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renCommon.entity.RenBoEntity;

/**
 * Created by Skye on 2018/12/12.
 */
public interface RenBoEntityRepository extends JpaRepository<RenBoEntity, String> {

    RenBoEntity findByPidAndBoName(String pid, String boName);
}
