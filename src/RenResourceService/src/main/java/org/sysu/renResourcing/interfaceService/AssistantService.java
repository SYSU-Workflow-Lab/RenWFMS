package org.sysu.renResourcing.interfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renResourcing.entity.RenRuntimerecordEntity;
import org.sysu.renResourcing.dao.RenRuntimerecordEntityDAO;
import org.sysu.renResourcing.utility.LogUtil;

/**
 * Created by Skye on 2018/12/13.
 */

@Service
public class AssistantService {

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Transactional(rollbackFor = Exception.class)
    public RenRuntimerecordEntity IBfindRuntimerecordEntityByRtid(String rtid) {
        try {
            return renRuntimerecordEntityDAO.findByRtid(rtid);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("PerformEngineSubmitTask get Runtime record failed. " + ex,
                    InterfaceB.class.getName(), LogLevelType.ERROR, rtid);
            throw ex;
        }
    }

}
