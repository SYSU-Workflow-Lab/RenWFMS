/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.utility.EncryptUtil;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renNameService.GlobalContext;
import org.sysu.renNameService.dao.RenAuthuserEntityDAO;
import org.sysu.renNameService.dao.RenSessionEntityDAO;
import org.sysu.renCommon.entity.RenAuthuserEntity;
import org.sysu.renCommon.entity.multikeyclass.RenAuthuserEntityMKC;
import org.sysu.renCommon.entity.RenSessionEntity;
import org.sysu.renNameService.utility.LogUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Author: Rinkako
 * Date  : 2018/1/28
 * Usage : This class maintaining authorization of service request token.
 */

@Service
public class AuthTokenManager {


    @Autowired
    private RenAuthuserEntityDAO renAuthuserEntityDAO;

    @Autowired
    private RenSessionEntityDAO renSessionEntityDAO;

    /**
     * Request for a auth token by authorization user info.
     * @param username user unique id, with domain name
     * @param password password
     * @return a token if authorization success, otherwise a string start with `#` for failure reason
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public String Auth(String username, String password) {
        try {
            // verify username and password
            String encryptedPassword = EncryptUtil.EncryptSHA256(password);
            String[] authItem = username.split("@");
            if (authItem.length != 2) {
                return "#user_not_valid";
            }
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setUsername(authItem[0]);
            mkc.setDomain(authItem[1]);
            RenAuthuserEntity rae = renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);

            if (rae == null || rae.getStatus() != 0) {
                return "#user_not_valid";
            }
            else if (!rae.getPassword().equals(encryptedPassword)) {
                return "#password_invalid";
            }
            // check if active session exist, ban it
            List<RenSessionEntity> oldRseList = renSessionEntityDAO.findRenSessionEntitiesByUsernameAndDestroyTimestampIsNotNull(username);
            Timestamp currentTS = TimestampUtil.GetCurrentTimestamp();
            for (RenSessionEntity rse : oldRseList) {
                if (rse.getUntilTimestamp().after(currentTS)) {
                    rse.setDestroyTimestamp(currentTS);
                    renSessionEntityDAO.saveOrUpdate(rse);
                }
            }
            // create new session
            String tokenId = String.format("AUTH_%s_%s", username, UUID.randomUUID());
            RenSessionEntity rse = new RenSessionEntity();
            long createTs = System.currentTimeMillis();
            rse.setLevel(rae.getLevel());
            rse.setToken(tokenId);
            rse.setUsername(username);
            rse.setCreateTimestamp(new Timestamp(createTs));
            if (GlobalContext.AUTHORITY_TOKEN_VALID_SECOND != 0) {
                rse.setUntilTimestamp(new Timestamp(createTs + 1000 * GlobalContext.AUTHORITY_TOKEN_VALID_SECOND));
            }
            renSessionEntityDAO.saveOrUpdate(rse);
            return tokenId;
        }
        catch (Exception ex) {
            LogUtil.Log(String.format("Request for auth but exception occurred (%s), service rollback, %s", username, ex),
                    AuthTokenManager.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "#exception_occurred";
        }
    }

    /**
     * Get domain of a token.
     * @param token auth token
     * @return domain name, null if invalid
     */
    public String GetDomain(String token) {
        String[] tokenItem = token.split("_");
        if (tokenItem.length != 3) {
            return null;
        }
        String[] authNameItem = tokenItem[1].split("@");
        if (authNameItem.length != 2) {
            return null;
        }
        return authNameItem[1];
    }

    /**
     * Destroy a token.
     * @param token auth token
     */
    @Transactional(rollbackFor = Exception.class)
    public void Destroy(String token) {
        try {
            RenSessionEntity rse = renSessionEntityDAO.findByToken(token);
            if (rse == null) {
                return;
            }
            rse.setDestroyTimestamp(TimestampUtil.GetCurrentTimestamp());
            renSessionEntityDAO.saveOrUpdate(rse);
        }
        catch (Exception ex) {
            LogUtil.Log(String.format("Destroy auth but exception occurred (%s), service rollback, %s", token, ex),
                    AuthTokenManager.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * Check if a token is valid.
     * @param token auth token to be checked
     * @return whether token is valid
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean CheckValid(String token) {
        // internal service call
        if (token.equals(GlobalContext.INTERNAL_TOKEN)) {
            return true;
        }
        boolean retFlag = true;
        try {
            RenSessionEntity rse = renSessionEntityDAO.findByToken(token);
            if (rse == null || rse.getDestroyTimestamp() != null ||
                rse.getUntilTimestamp().before(TimestampUtil.GetCurrentTimestamp())) {
                retFlag = false;
            }
        }
        catch (Exception ex) {
            LogUtil.Log(String.format("Check auth validation but exception occurred (%s), service rollback, %s", token, ex),
                    AuthTokenManager.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            retFlag = false;
        }
        return retFlag;
    }

    /**
     * Check if a token is valid and get level.
     * @param token auth token to be checked
     * @return token level, -1 if token is invalid
     */
    @Transactional(rollbackFor = Exception.class)
    public int CheckValidLevel(String token) {
        // internal service call
        if (token.equals(GlobalContext.INTERNAL_TOKEN)) {
            return 999;
        }
        int retVal;
        try {
            RenSessionEntity rse = renSessionEntityDAO.findByToken(token);
            if (rse == null || rse.getDestroyTimestamp() != null ||
                    rse.getUntilTimestamp().before(TimestampUtil.GetCurrentTimestamp())) {
                retVal = -1;
            }
            else {
                retVal = rse.getLevel();
            }
        }
        catch (Exception ex) {
            LogUtil.Log(String.format("Check auth validation but exception occurred (%s), service rollback, %s", token, ex),
                    AuthTokenManager.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            retVal = -1;
        }
        return retVal;
    }
}
