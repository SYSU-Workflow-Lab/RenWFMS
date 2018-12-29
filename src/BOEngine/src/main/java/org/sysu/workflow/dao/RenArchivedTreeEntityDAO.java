package org.sysu.workflow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.sysu.renCommon.entity.RenArchivedTreeEntity;
import org.sysu.workflow.repository.RenArchivedTreeEntityRepository;

/**
 * Created by Skye on 2018/12/23.
 */

@Repository
@CacheConfig(cacheNames = "ren_archived_tree")
public class RenArchivedTreeEntityDAO {

    @Autowired
    private RenArchivedTreeEntityRepository renArchivedTreeEntityRepository;

    @Cacheable(key = "#p0")
    public RenArchivedTreeEntity findByRtid(String rtid) {
        return renArchivedTreeEntityRepository.findOne(rtid);
    }


    @CachePut(key = "#p0.rtid")
    public RenArchivedTreeEntity saveOrUpdate(RenArchivedTreeEntity renArchivedTreeEntity) {
        return renArchivedTreeEntityRepository.saveAndFlush(renArchivedTreeEntity);
    }

}
