package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenLogEntity;
import org.sysu.renResourcing.repository.RenLogEntityRepository;

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
