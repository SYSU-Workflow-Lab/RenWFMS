package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renResourcing.entity.RenQueueitemsEntity;
import org.sysu.renResourcing.entity.multikeyclass.RenQueueitemsEntityMKC;

import java.util.List;

/**
 * Created by Skye on 2018/12/22.
 */
public interface RenQueueitemsEntityRepository extends JpaRepository<RenQueueitemsEntity, RenQueueitemsEntityMKC> {

    List<RenQueueitemsEntity> findRenQueueitemsEntitiesByWorkitemId(String workitemId);

    List<RenQueueitemsEntity> findRenQueueitemsEntitiesByWorkqueueId(String workqueueId);

    void deleteByWorkitemId(String wid);

    void deleteByWorkqueueIdAndWorkitemId(String workqueueId, String wid);

    RenQueueitemsEntity findByWorkqueueIdAndWorkitemId(String workqueueId, String wid);

}
