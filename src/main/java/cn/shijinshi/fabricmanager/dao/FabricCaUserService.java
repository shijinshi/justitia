package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.entity.User;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.dao.mapper.FabricCaUserMapper;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.utils.StringConverter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FabricCaUserService {
    private static final Logger LOGGER = Logger.getLogger(FabricCaUserService.class);

    @Autowired
    private FabricCaUserMapper mapper;
    @Autowired
    private CertificatesService certService;
    @Autowired
    private UserService userService;

    @Autowired
    private StringConverter converter;


    public int insertUser(FabricCaUser user) {
        user.setSecret(converter.encrypt(user.getSecret()));
        int res = 0;
        try {
            res = mapper.insertSelective(user);
        } catch (DuplicateKeyException e) {     //主键冲突，忽略这个数据
//            LOGGER.debug(e);
        }
        return res;
    }

    public FabricCaUser getCaUser(String caUserId, String serverName) throws NotFoundBySqlException {
        FabricCaUser user = mapper.selectByPrimaryKey(caUserId, serverName);
        if (user == null) throw new NotFoundBySqlException("Cannot get data by primary key");

        user.setSecret(converter.decrypt(user.getSecret()));
        return user;
    }

    public List<UserAndCerts> selectUserCerts(String caUserId, String serverName) {
        List<UserAndCerts> userCerts = mapper.getUserCerts(caUserId, serverName);
        if (userCerts != null && !userCerts.isEmpty()) {
            for (UserAndCerts userAndCerts : userCerts) {
                userAndCerts.setSecret(converter.decrypt(userAndCerts.getSecret()));
            }
        }
        return userCerts;
    }

    public UserAndCerts getUserCerts(String caUserId, String serverName) throws NotFoundBySqlException {
        List<UserAndCerts> usersCerts = mapper.getUserCerts(caUserId, serverName);
        if (usersCerts == null || usersCerts.isEmpty()) {
            throw new NotFoundBySqlException("Cannot get data by primary key");
        }

        UserAndCerts userCerts = usersCerts.get(0);
        Certificates certificate = userCerts.getCertificate();
        if (certificate != null) {
            if (!Certificates.STATE_GOOD.equals(certificate.getState())) {
                throw new ServiceException("CA用户(" + serverName + ":" + caUserId + ")证书已失效");
            }
            Date notAfter = certificate.getNotAfter();
            if (notAfter != null && notAfter.getTime() < System.currentTimeMillis()) {
                throw new ServiceException("CA用户(" + serverName + ":" + caUserId + ")证书已过期");
            }
        } else {
            throw new ServiceException("CA用户(" + serverName + ":" + caUserId + ")没有登记证书，请先登记证书");
        }

        userCerts.setSecret(converter.decrypt(userCerts.getSecret()));
        return userCerts;
    }

    public List<UserAndCerts> selectOrgAdminUser() {
        return mapper.selectOrgAdminUser();
    }

    public List<String> selectCaAdminUser(String serverName) {
        return mapper.selectCaAdminUser(serverName);
    }

    public int deleteByServer(String serverName) {
        return mapper.deleteByServer(serverName);
    }

    @Transactional
    public int deleteUser(String caUserId, String serverName) {
        certService.deleteCertByCAUser(caUserId, serverName);
        return mapper.deleteByPrimaryKey(caUserId, serverName);
    }

    public int updateUserState(String caUserId, String serverName, String state) {
        return mapper.updateUserState(caUserId, serverName, state);
    }


    /**
     * 根据用户ID获取自己及其子用户有权限使用的ca user信息
     *
     * @param requester 用户id
     * @return
     */
    public List<UserAndCerts> selectByRequester(String requester) {
        User user = userService.findUserById(requester);
        String affiliationLike = user.getAffiliation() + ".%";
        return mapper.selectByRequester(user.getAffiliation(), affiliationLike);
    }

    public int updateTlsCert(FabricCaUser user) {
        return mapper.updateTlsCert(user);
    }
}