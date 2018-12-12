package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renNameService.entity.RenSessionEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/11.
 */
public interface RenSessionEntityRepository extends JpaRepository<RenSessionEntity, String> {

    List<RenSessionEntity> findRenSessionEntitiesByUsernameAndDestroyTimestampIsNotNull(String username);

}
