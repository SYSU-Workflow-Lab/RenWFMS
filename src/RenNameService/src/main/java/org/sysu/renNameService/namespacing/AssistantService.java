package org.sysu.renNameService.namespacing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.utility.AuthDomainHelper;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renNameService.dao.RenBoEntityDAO;
import org.sysu.renNameService.dao.RenProcessEntityDAO;
import org.sysu.renNameService.dao.RenRuntimerecordEntityDAO;
import org.sysu.renNameService.entity.RenBoEntity;
import org.sysu.renNameService.entity.RenProcessEntity;
import org.sysu.renNameService.entity.RenRuntimerecordEntity;
import org.sysu.renNameService.utility.LogUtil;

/**
 * Created by Skye on 2018/12/13.
 */

@Service
public class AssistantService {

    @Autowired
    private RenBoEntityDAO renBoEntityDAO;

    @Autowired
    private RenRuntimerecordEntityDAO renRuntimerecordEntityDAO;

    @Autowired
    private RenProcessEntityDAO renProcessEntityDAO;

    @Transactional(rollbackFor = Exception.class)
    public boolean newBo(String boid, String pid, String name, String content) {
        try {
            RenBoEntity rbe = new RenBoEntity();
            rbe.setBoid(boid);
            rbe.setPid(pid);
            rbe.setBoName(name);
            rbe.setBoContent(content);
            renBoEntityDAO.saveOrUpdate(rbe);
            return true;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Upload BO but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean startProcess(String rtid) {
        try {
            RenRuntimerecordEntity rrte = renRuntimerecordEntityDAO.findByRtid(rtid);
            rrte.setLaunchTimestamp(TimestampUtil.GetCurrentTimestamp());
            String launcher = AuthDomainHelper.GetAuthNameByRTID(rtid);
            rrte.setLaunchAuthorityId(launcher);
            renRuntimerecordEntityDAO.saveOrUpdate(rrte);
            RenProcessEntity rpe = renProcessEntityDAO.findByPid(rrte.getProcessId());
            rpe.setLaunchCount(rpe.getLaunchCount() + 1);
            rpe.setLastLaunchTimestamp(TimestampUtil.GetCurrentTimestamp());
            renProcessEntityDAO.saveOrUpdate(rpe);
            return true;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.Log("Start process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            return false;
        }
    }

}
