package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenLogEntity;
import org.sysu.workflow.repository.RenLogEntityRepository;

/**
 * Created by Skye on 2018/12/13.
 */

@Repository
public class RenLogEntityDAO {

    @Autowired
    private RenLogEntityRepository renLogEntityRepository;

    public RenLogEntity saveOrUpdate(RenLogEntity renLogEntity) {
        return renLogEntityRepository.saveAndFlush(renLogEntity);
    }

}
