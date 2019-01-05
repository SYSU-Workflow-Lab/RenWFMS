package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenServiceInfo;

import java.util.List;

/**
 * Created by Skye on 2019/1/2.
 */
public interface RenServiceInfoRepository extends JpaRepository<RenServiceInfo, String> {

    @Query(value = "select location from ren_serviceinfo", nativeQuery = true)
    List<String> findAllLocation();

}
