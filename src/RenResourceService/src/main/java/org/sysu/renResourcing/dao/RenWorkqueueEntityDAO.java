package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenWorkqueueEntity;
import org.sysu.renResourcing.repository.RenWorkqueueEntityRepository;

/**
 * Created by Skye on 2018/12/22.
 */

@Repository
@CacheConfig(cacheNames = "rs_ren_workqueue")
public class RenWorkqueueEntityDAO {

    @Autowired
    private RenWorkqueueEntityRepository renWorkqueueEntityRepository;

    public RenWorkqueueEntity saveOrUpdate(RenWorkqueueEntity renWorkqueueEntity) {
        return renWorkqueueEntityRepository.saveAndFlush(renWorkqueueEntity);
    }

    public RenWorkqueueEntity findByOwnerIdAndType(String ownerId, int type) {
        return renWorkqueueEntityRepository.findByOwnerIdAndType(ownerId, type);
    }

}
