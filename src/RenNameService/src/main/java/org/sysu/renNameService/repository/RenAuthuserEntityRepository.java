package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renCommon.entity.RenAuthuserEntity;
import org.sysu.renCommon.entity.multikeyclass.RenAuthuserEntityMKC;

import java.util.List;

/**
 * Created by Skye on 2018/12/11.
 */
public interface RenAuthuserEntityRepository extends JpaRepository<RenAuthuserEntity, RenAuthuserEntityMKC> {

    List<RenAuthuserEntity> findRenAuthuserEntitiesByDomain(String domain);

}
