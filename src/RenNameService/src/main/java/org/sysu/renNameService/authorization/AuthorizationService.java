/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.sysu.renCommon.utility.CommonUtil;
import org.sysu.renCommon.utility.EncryptUtil;
import org.sysu.renCommon.utility.TimestampUtil;
import org.sysu.renNameService.GlobalContext;
import org.sysu.renNameService.dao.RenAuthuserEntityDAO;
import org.sysu.renNameService.dao.RenDomainEntityDAO;
import org.sysu.renNameService.dao.RenWorkitemEntityDAO;
import org.sysu.renNameService.entity.RenAuthuserEntity;
import org.sysu.renNameService.entity.multikeyclass.RenAuthuserEntityMKC;
import org.sysu.renNameService.entity.RenDomainEntity;
import org.sysu.renNameService.entity.RenWorkitemEntity;
import org.sysu.renNameService.utility.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Rinkako
 * Date  : 2018/1/28
 * Usage : All BO environment authorization services will be handled in this service module.
 */

@Service
public class AuthorizationService {

    @Autowired
    private AuthTokenManager authTokenManager;

    @Autowired
    private RenDomainEntityDAO renDomainEntityDAO;

    @Autowired
    private RenAuthuserEntityDAO renAuthuserEntityDAO;

    @Autowired
    private RenWorkitemEntityDAO renWorkitemEntityDAO;

    /**
     * Connect and get a auth token for BO environment user.
     *
     * @param username user unique name
     * @param password password without encryption
     * @return a token if authorization success, otherwise a string start with `#` for failure reason
     */
    public String Connect(String username, String password) {
        return authTokenManager.Auth(username, password);
    }

    /**
     * Disconnect and destroy the auth token.
     *
     * @param token token to be destroy
     */
    public void Disconnect(String token) {
        authTokenManager.Destroy(token);
    }

    /**
     * Check if a token valid.
     *
     * @param token token to be checked
     * @return boolean of validation
     */
    public boolean CheckValid(String token) {
        return authTokenManager.CheckValid(token);
    }

    /**
     * Check if a token is valid and get its level.
     *
     * @param token token to be checked
     * @return token level, -1 if token is invalid
     */
    public int CheckValidLevel(String token) {
        return authTokenManager.CheckValidLevel(token);
    }

    /**
     * Add a domain.
     *
     * @param name          domain unique name
     * @param password      domain admin password
     * @param level         domain level
     * @param corganGateway binding COrgan gateway URL
     * @return domain private signature key
     */
    @Transactional(rollbackFor = Exception.class)
    public String AddDomain(String name, String password, String level, String corganGateway) {
        try {
            // check existence
            RenDomainEntity existRae = renDomainEntityDAO.findByName(name);
            if (existRae != null || name.trim().equals("")) {
                LogUtil.Log(String.format("AddAuthorizationUser but username already exist (%s), service rollback.", name),
                        AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
                return "#duplicate_domain";
            }
            // create new domain
            Timestamp createTs = TimestampUtil.GetCurrentTimestamp();
            RenDomainEntity rde = new RenDomainEntity();
            rde.setName(name);
            rde.setLevel(Integer.valueOf(level));
            rde.setCorganGateway(corganGateway);
            rde.setStatus(0);
            rde.setLevel(0);
            rde.setCreatetimestamp(createTs);
            String signature = RSASignatureUtil.Signature(name, GlobalContext.PRIVATE_KEY);
            assert signature != null;
            String safeSignature = RSASignatureUtil.SafeUrlBase64Encode(signature);
            rde.setUrlsafeSignature(safeSignature);
            renDomainEntityDAO.saveOrUpdate(rde);
            // create admin auth user
            RenAuthuserEntity rae = new RenAuthuserEntity();
            rae.setUsername(GlobalContext.DOMAIN_ADMIN_NAME);
            rae.setDomain(name);
            rae.setStatus(0);
            rae.setCreatetimestamp(createTs);
            rae.setPassword(EncryptUtil.EncryptSHA256(password));
            rae.setLevel(1);
            renAuthuserEntityDAO.saveOrUpdate(rae);
            return safeSignature;
        } catch (Exception ex) {
            LogUtil.Log(String.format("Add domain but exception occurred (%s), service rollback, %s", name, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "#exception";
        }
    }

    /**
     * Disable a domain, make it unable to connect.
     *
     * @param name domain unique name
     * @return boolean of whether execution success
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean RemoveDomain(String name) {
        try {
            RenDomainEntity rde = renDomainEntityDAO.findByName(name);
            if (rde != null) {
                rde.setStatus(1);
                renDomainEntityDAO.saveOrUpdate(rde);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            LogUtil.Log(String.format("Remove domain but exception occurred (%s), service rollback, %s", name, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * Update a domain.
     *
     * @param name       domain unique name
     * @param updateArgs update argument name-value dictionary
     * @param isAdmin    is operator WFMS admin
     * @return boolean of whether execution success
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean UpdateDomain(String name, HashMap<String, String> updateArgs, Boolean isAdmin) {
        try {
            RenDomainEntity rde = renDomainEntityDAO.findByName(name);
            if (rde == null) {
                return false;
            }
            if (updateArgs.containsKey("corgan")) {
                rde.setCorganGateway(updateArgs.get("corgan"));
            }
            if (updateArgs.containsKey("status") && isAdmin) {
                rde.setStatus(Integer.valueOf(updateArgs.get("status")));
            }
            if (updateArgs.containsKey("level") && isAdmin) {
                rde.setLevel(Integer.valueOf(updateArgs.get("level")));
            }
            renDomainEntityDAO.saveOrUpdate(rde);
            return true;
        } catch (Exception ex) {
            LogUtil.Log(String.format("Update domain but exception occurred (%s), service rollback, %s", name, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * Check if a domain is already exist.
     *
     * @param name domain unique name to be checked
     * @return boolean of existence
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean ContainDomain(String name) {
        try {
            RenDomainEntity rae = renDomainEntityDAO.findByName(name);
            return rae != null;
        } catch (Exception ex) {
            LogUtil.Log(String.format("Contain domain check but exception occurred (%s), service rollback, %s", name, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return true;
        }
    }

    /**
     * Retrieve a domain.
     *
     * @param name domain unique name
     * @return {@code RenAuthEntity} instance
     */
    @Transactional(rollbackFor = Exception.class)
    public RenDomainEntity RetrieveDomain(String name) {
        try {
            return renDomainEntityDAO.findByName(name);
        } catch (Exception ex) {
            LogUtil.Log(String.format("Retrieve domain but exception occurred (%s), service rollback, %s", name, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Retrieve all domain.
     *
     * @return {@code RenAuthEntity} instance
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RenDomainEntity> RetrieveAllDomain() {
        try {
            List<RenDomainEntity> rae = renDomainEntityDAO.findAll();
            return rae;
        } catch (Exception ex) {
            LogUtil.Log(String.format("Retrieve all domain but exception occurred, service rollback, %s", ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Add a auth user.
     *
     * @param username user unique name
     * @param password user password without encryption
     * @param level    user level
     * @param domain   domain name
     * @param gid      global id of binding resources
     * @return `OK` if success otherwise failed
     */
    @Transactional(rollbackFor = Exception.class)
    public String AddAuthUser(String username, String password, int level, String domain, String gid) {
        try {
            // check existence
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setDomain(domain);
            mkc.setUsername(username);
            RenAuthuserEntity existRae = renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);
            if (existRae != null) {
                LogUtil.Log(String.format("AddAuthorizationUser but username already exist (%s@%s), service rollback.", username, domain),
                        AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
                return "#duplicate_username";
            }
            // create new
            RenAuthuserEntity rae = new RenAuthuserEntity();
            rae.setUsername(username);
            rae.setLevel(level);
            rae.setPassword(EncryptUtil.EncryptSHA256(password));
            rae.setDomain(domain);
            rae.setCreatetimestamp(TimestampUtil.GetCurrentTimestamp());
            rae.setStatus(0);
            rae.setGid(gid == null ? "" : gid);
            renAuthuserEntityDAO.saveOrUpdate(rae);
            return "OK";
        } catch (Exception ex) {
            LogUtil.Log(String.format("AddAuthorizationUser but exception occurred (%s@%s), service rollback, %s", username, domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "#exception";
        }
    }

    /**
     * Disable a BO environment user, make it unable to connect.
     *
     * @param username user unique name
     * @param domain   domain name
     * @return boolean of whether execution success
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean RemoveAuthorizationUser(String username, String domain) {
        try {
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setUsername(username);
            mkc.setDomain(domain);
            RenAuthuserEntity rae = renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);
            if (rae != null) {
                rae.setStatus(1);
                renAuthuserEntityDAO.saveOrUpdate(rae);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            LogUtil.Log(String.format("RemoveAuthorizationUser but exception occurred (%s@%s), service rollback, %s", username, domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * Update a BO environment user profile.
     *
     * @param username   user unique name
     * @param domain     domain name
     * @param updateArgs update argument name-value dictionary
     * @return boolean of whether execution success
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean UpdateAuthorizationUser(String username, String domain, HashMap<String, String> updateArgs, Boolean isAdmin) {
        try {
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setDomain(domain);
            mkc.setUsername(username);
            RenAuthuserEntity rae = renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);
            if (rae == null) {
                return false;
            }
            if (updateArgs.containsKey("password")) {
                rae.setPassword(EncryptUtil.EncryptSHA256(updateArgs.get("password")));
            }
            if (updateArgs.containsKey("gid")) {
                rae.setGid(updateArgs.get("gid"));
            }
            if (updateArgs.containsKey("status") && isAdmin) {
                rae.setStatus(Integer.valueOf(updateArgs.get("status")));
            }
            if (updateArgs.containsKey("level") && isAdmin) {
                rae.setLevel(Integer.valueOf(updateArgs.get("level")));
            }
            renAuthuserEntityDAO.saveOrUpdate(rae);
            return true;
        } catch (Exception ex) {
            LogUtil.Log(String.format("UpdateAuthorizationUser but exception occurred (%s@%s), service rollback, %s", username, domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * Check if a BO environment user is already exist.
     *
     * @param username user unique name to be checked
     * @param domain   domain name
     * @return boolean of existence
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean ContainAuthorizationUser(String username, String domain) {
        try {
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setDomain(domain);
            mkc.setUsername(username);
            RenAuthuserEntity rae = renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);
            return rae != null;
        } catch (Exception ex) {
            LogUtil.Log(String.format("ContainAuthorizationUser but exception occurred (%s@%s), service rollback, %s", username, domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return true;
        }
    }

    /**
     * Retrieve a BO environment user.
     *
     * @param username user unique name to be checked
     * @param domain   domain which user in
     * @return {@code RenAuthEntity} instance
     */
    @Transactional(rollbackFor = Exception.class)
    public RenAuthuserEntity RetrieveAuthorizationUser(String username, String domain) {
        try {
            RenAuthuserEntityMKC mkc = new RenAuthuserEntityMKC();
            mkc.setDomain(domain);
            mkc.setUsername(username);
            return renAuthuserEntityDAO.findByRenAuthuserEntityMKC(mkc);
        } catch (Exception ex) {
            LogUtil.Log(String.format("RetrieveAuthorizationUser but exception occurred (%s@%s), service rollback, %s", username, domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Retrieve all BO environment users.
     *
     * @return {@code RenAuthEntity} instance
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RenAuthuserEntity> RetrieveAllAuthorizationUser(String domain) {
        try {
            List<RenAuthuserEntity> rae;
            if (CommonUtil.IsNullOrEmpty(domain)) {
                rae = renAuthuserEntityDAO.findAll();
            } else {
                rae = renAuthuserEntityDAO.findRenAuthuserEntitiesByDomain(domain);
            }
            return rae;
        } catch (Exception ex) {
            LogUtil.Log(String.format("RetrieveAllAuthorizationUser but exception occurred (%s), service rollback, %s", domain, ex),
                    AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    /**
     * Check if a workitem belong to signature owner domain.
     *
     * @param signature  signature key string
     * @param workitemId workitem global id
     * @return boolean of check result
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean CheckWorkitemSignature(String signature, String workitemId) {
        boolean cmtFlag = false;
        try {
            RenWorkitemEntity rwe = renWorkitemEntityDAO.findByWid(workitemId);
            cmtFlag = true;
            String rtid = rwe.getRtid();
            return this.CheckRTIDSignature(signature, rtid);
        } catch (Exception ex) {
            if (!cmtFlag) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            ex.printStackTrace();
            LogUtil.Log(String.format("CheckWorkitemSignature(KEY:%s, WID:%s) but exception occurred, %s",
                    signature, workitemId, ex), AuthorizationService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            throw ex;
        }
    }

    /**
     * Get the owner domain of signature.
     *
     * @param signature signature key string
     * @return owner domain name, null if not exist
     * todo process private signature key check
     */
    private String GetSignatureOwner(String signature) {
        RenDomainEntity domain = renDomainEntityDAO.findByUrlsafeSignature(signature);
        return domain == null ? null : domain.getName();
    }

    /**
     * Check if a workitem belong to signature owner domain.
     *
     * @param signature signature key string
     * @param rtid      process rtid
     * @return boolean of check result
     */
    public boolean CheckRTIDSignature(String signature, String rtid) {
        String owner = this.GetSignatureOwner(signature);
        String rtidDomain = rtid.split("_")[1].split("@")[1];
        return owner != null && owner.equals(rtidDomain);
    }
}