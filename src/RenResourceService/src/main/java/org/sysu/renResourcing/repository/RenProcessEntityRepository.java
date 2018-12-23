package org.sysu.renResourcing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renResourcing.entity.RenProcessEntity;

import java.util.List;

/**
 * Created by Skye on 2018/12/12.
 */
public interface RenProcessEntityRepository extends JpaRepository<RenProcessEntity, String> {
}
