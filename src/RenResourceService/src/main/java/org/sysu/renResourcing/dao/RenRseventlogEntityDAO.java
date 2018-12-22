package org.sysu.renResourcing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renResourcing.entity.RenRseventlogEntity;
import org.sysu.renResourcing.repository.RenRseventlogEntityRepository;

/**
 * Created by Skye on 2018/12/21.
 */

@Repository
public class RenRseventlogEntityDAO {

    @Autowired
    private RenRseventlogEntityRepository renRseventlogEntityRepository;

    public RenRseventlogEntity saveOrUpdate(RenRseventlogEntity renRseventlogEntity) {
        return renRseventlogEntityRepository.saveAndFlush(renRseventlogEntity);
    }

}
