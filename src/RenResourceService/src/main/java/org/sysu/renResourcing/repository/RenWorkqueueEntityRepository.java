package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renCommon.entity.RenWorkqueueEntity;

/**
 * Created by Skye on 2018/12/22.
 */
public interface RenWorkqueueEntityRepository extends JpaRepository<RenWorkqueueEntity, String> {

    RenWorkqueueEntity findByOwnerIdAndType(String ownerId, int type);

}
