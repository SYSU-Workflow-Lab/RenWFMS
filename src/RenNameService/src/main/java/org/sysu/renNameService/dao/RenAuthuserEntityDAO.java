package org.sysu.renNameService.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sysu.renNameService.entity.RenAuthuserEntity;
import org.sysu.renNameService.entity.multikeyclass.RenAuthuserEntityMKC;
import org.sysu.renNameService.repository.RenAuthuserEntityRepository;

import java.util.List;

/**
 * Created by Skye on 2018/12/11.
 */

@Repository
public class RenAuthuserEntityDAO {

    @Autowired
    private RenAuthuserEntityRepository renAuthuserEntityRepository;

    public RenAuthuserEntity saveOrUpdate(RenAuthuserEntity renAuthuserEntity) {
        return renAuthuserEntityRepository.save(renAuthuserEntity);
    }

    public List<RenAuthuserEntity> findAll() {
        return renAuthuserEntityRepository.findAll();
    }

    public List<RenAuthuserEntity> findRenAuthuserEntitiesByDomain(String domain) {
        return renAuthuserEntityRepository.findRenAuthuserEntitiesByDomain(domain);
    }

    public RenAuthuserEntity findByRenAuthuserEntityMKC(RenAuthuserEntityMKC renAuthuserEntityMKC) {
        return renAuthuserEntityRepository.findOne(renAuthuserEntityMKC);
    }

}
