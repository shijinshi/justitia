package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.controller.entity.fabricca.request.EnrollEntity;
import cn.shijinshi.fabricmanager.controller.entity.fabricca.request.ReenrollEntity;
import cn.shijinshi.fabricmanager.dao.*;
import cn.shijinshi.fabricmanager.dao.entity.*;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.ca.FabricCaRequester;
import cn.shijinshi.fabricmanager.service.ca.entity.*;
import cn.shijinshi.fabricmanager.service.fabric.FabricUserImpl;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.FabricCaManager;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.*;
import org.hyperledger.fabric_ca.sdk.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class FabricCaRequestService {
    private static final Logger LOGGER = Logger.getLogger(FabricCaRequestService.class);

    @Autowired
    private FabricCaRequester requester;
    @Autowired
    private FabricCaUserService fabricCaUserService;
    @Autowired
    private FabricCaServerService serverService;
    @Autowired
    private HostService hostService;
    @Autowired
    private CertificatesService certService;

    @Autowired
    private PeerNodeService peerNodeService;
    @Autowired
    private OrdererNodeService ordererNodeService;
    @Autowired
    private UserService userService;


    /**
     * 在fabric ca 上注册一个身份，不生成相关证书
     *
     * @param registerInfo 注册一个身份所需要的信息
     */
    public void registerIdentity(String serverName, String creator, String parentUserId, RegisterInfo registerInfo) {
        if (Organization.ORG_TYPE_ORDERER.equals(Context.getOrganization().getOrgType()) && "peer".equals(registerInfo.getUserType())) {
            throw new ServiceException("Orderer组织不支持注册peer类型用户");
        } else if (Organization.ORG_TYPE_PEER.equals(Context.getOrganization().getOrgType()) && "orderer".equals(registerInfo.getUserType())) {
            throw new ServiceException("Peer组织不支持注册orderer类型用户");
        }

        //获取父用户（注册者）的身份
        User parentUser = getFabricUserById(parentUserId, serverName);
        HFCAClient client = createClient(serverName);

        //注册新的身份
        if (StringUtils.isEmpty(registerInfo.getAffiliation())) {
            registerInfo.setAffiliation(parentUser.getAffiliation());
        }
        Map<String, Object> attributes = registerInfo.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, "*");             //hf.Registrar.Roles
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARDELEGATEROLES, "*");     //hf.Registrar.DelegateRoles
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES, "*");        //hf.Registrar.Attributes
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFINTERMEDIATECA, true);            //hf.IntermediateCA
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREVOKER, true);                   //hf.Revoker
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFAFFILIATIONMGR, true);            //hf.AffiliationMgr
            attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFGENCRL, true);                    //hf.GenCRL
        } else {
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARROLES, "*");             //hf.Registrar.Roles
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARDELEGATEROLES)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARDELEGATEROLES, "*");     //hf.Registrar.DelegateRoles
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREGISTRARATTRIBUTES, "*");        //hf.Registrar.Attributes
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFINTERMEDIATECA)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFINTERMEDIATECA, true);            //hf.IntermediateCA
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFREVOKER)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFREVOKER, true);                   //hf.Revoker
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFAFFILIATIONMGR)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFAFFILIATIONMGR, true);            //hf.AffiliationMgr
            }
            if (!attributes.containsKey(HFCAClient.HFCA_ATTRIBUTE_HFGENCRL)) {
                attributes.put(HFCAClient.HFCA_ATTRIBUTE_HFGENCRL, true);                    //hf.GenCRL
            }
        }
        String resSecret;
        try {
            resSecret = requester.register(registerInfo, parentUser, client);
        } catch (RegistrationException e) {
            String identityId = registerInfo.getIdentityId();
            if (e.getMessage() != null && e.getMessage().contains("Identity \'" + identityId + "\' is already registered")) {
                throw new ServiceException("CA用户(" + identityId + ")注册失败,用户" + identityId + "已被注册过");
            }
            throw new ServiceException("CA用户(" + identityId + ")注册失败", e);
        } catch (InvalidArgumentException e) {
            throw new ServiceException("注册CA用户参数异常", e);
        }
        String secret = registerInfo.getSecret();
        if (!resSecret.equals(secret)) {
            throw new ServiceException("Password exception you set：" + secret + "result：" + resSecret);
        }
        //保存新的身份信息到数据库
        saveCaUser(serverName, registerInfo, creator);
    }

    private FabricUserImpl getFabricUserById(String caUserId, String serverName) {
        UserAndCerts userCerts = checkUser(caUserId, serverName, true);
        Certificates certificate = userCerts.getCertificate();
        String certPem = certificate.getCertPem();
        String keyPem = certificate.getKeyPem();

        FabricUserImpl fabricUser = new FabricUserImpl(userCerts.getUserId(), userCerts.getAffiliation());
        try {
            fabricUser.setEnrollment(certPem, keyPem);
        } catch (IOException e) {
            throw new ServiceException("用户(" + serverName + ":" + caUserId + ")证书解析失败", e);
        }
        return fabricUser;
    }


    /**
     * 保存新注册的CA用户信息到数据库
     */
    private void saveCaUser(String serverName, RegisterInfo registerInfo, String creator) {
        FabricCaUser caUsers = new FabricCaUser();
        caUsers.setUserId(registerInfo.getIdentityId());
        caUsers.setServerName(serverName);
        caUsers.setSecret(registerInfo.getSecret());
        caUsers.setCreator(creator);
        String owner = registerInfo.getOwner();
        if (StringUtils.isEmpty(owner)) {
            caUsers.setOwner(creator);
        } else {
            caUsers.setOwner(owner);
        }
        caUsers.setUserType(registerInfo.getUserType());
        if (StringUtils.isEmpty(registerInfo.getIdentityType())) {
            caUsers.setIdentityType("member");
        } else {
            caUsers.setIdentityType(registerInfo.getIdentityType());
        }

        caUsers.setAffiliation(registerInfo.getAffiliation());
        Map<String, Object> attributes = registerInfo.getAttributes();
        if (attributes.containsKey("hf.Registrar.Roles")) {
            caUsers.setRoles((String) attributes.get("hf.Registrar.Roles"));
        }
        FabricCaManager.Attributes attr = new FabricCaManager.Attributes(attributes);
        caUsers.setAttributes(attr.toString());
        caUsers.setState(FabricCaUser.STATE_REGISTERED);
        caUsers.setMaxEnrollments(registerInfo.getMaxEnrollments());
        caUsers.setTlsEnable(Context.getOrganization().getTlsEnable());
        caUsers.setTlsCert(null);
        caUsers.setTlsCert(null);

        fabricCaUserService.insertUser(caUsers);
    }

    /**
     * 登记证书
     * 约定：为了便于证书管理和简化用户的配置流程，人为约束一个CA用户（任何身份类型）只允许一对有效的证书和私钥存在。
     * 若需要登记新的证书则必须把旧的证书注销掉。
     * <p>
     * 本地生成证书和私钥，通过这对公私钥生成CSR（Certificate Signing Request）发送给CA服务端签发一个证书到指定用户名下(CA服务端不保存私钥）
     */
    public User enrollIdentity(String caUserId, String serverName, EnrollEntity enroll) {

        UserAndCerts caUser = checkUser(caUserId, serverName, false);
        //注销原有证书
        Certificates certificate = caUser.getCertificate();
        if (certificate != null) {
            //todo 如果证书正在被使用的话，需要更新网络节点上的证书
            String serial = certificate.getSerialNumber();
            String aki = certificate.getAuthorityKeyIdentifier();
            RevokeInfo revokeInfo = new RevokeInfo(RevokeInfo.Reason.SUPERSEDED, false, serial, aki);
            revokeCert(caUserId, serverName, revokeInfo, null, true);
        }
        //登记新的证书
        EnrollInfo enrollInfo = new EnrollInfo();
        enrollInfo.setIdentityId(caUserId);
        enrollInfo.setSecret(caUser.getSecret());
        enrollInfo.setAffiliation(caUser.getAffiliation());
        enrollInfo.setExtend(enroll.getEnrollInfo());
        User user;
        try {
            user = requester.enroll(enrollInfo, createClient(serverName));
        } catch (EnrollmentException e) {
            LOGGER.error(e);
            throw new ServiceException("证书登记失败", e);
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("证书登记请求参数异常", e);
        }
        saveCert(user, caUserId, serverName);
        //如果是Peer或者orderer节点看是否需要TLS证书
        String userType = caUser.getUserType();
        if ("peer".equals(userType) || "orderer".equals(userType) || "user".equals(userType)) {
            Organization organization = Context.getOrganization();
            if (organization.getTlsEnable()) {
                CertFileHelper certFileHelper = new CertFileHelper();
                try {
                    enrollInfo.setProfile("tls");
                    User userTls = requester.enroll(enrollInfo, createClient(organization.getTlsCaServer()));
                    Enrollment enrollment = userTls.getEnrollment();
                    String keyPem = certFileHelper.privateKeyToPem(enrollment.getKey());
                    caUser.setTlsEnable(true);
                    caUser.setTlsKey(keyPem);
                    caUser.setTlsCert(enrollment.getCert());
                } catch (Exception e) {
                    LOGGER.error(e);
                    throw new ServiceException("用户(" + caUser.getServerName() + ":" + caUser.getUserId() + ")TLS证书签发失败");
                }
                fabricCaUserService.updateTlsCert(caUser);
            }
        }
        //todo 更新证书吊销列表到网络上

        if (FabricCaUser.STATE_REGISTERED.equals(caUser.getState())) {
            fabricCaUserService.updateUserState(caUserId, serverName, FabricCaUser.STATE_ENROLLED);
        }
        return user;
    }


    public void enrollIdentity(String caUserId, String serverName) {
        enrollIdentity(caUserId, serverName, new EnrollEntity());
    }

    /**
     * 重新登记证书，如果证书即将过期(已经过期的不行)可以通过该方法续期（生成新的证书），保留原来的公私钥不变
     * <p>
     * 约定：为了便于证书管理和简化用户的配置流程，人为约束一个CA用户只（任何身份类型）允许一对有效的证书和私钥存在。
     * 若需要登记新的证书则必须把旧的证书注销掉。
     * <p>
     * 本接口需要指定一个CA用户和此用户的一对证书和私钥。通过私钥和证书中的公钥生成新的CSR（Certificate Signing Request）
     * 发送给CA服务端签发一个证书到指定用户名下(CA服务端不保存私钥）。原有证书和私钥任然有效，若需要原有证书和私钥失效需要再调用revoke接口。
     */
    public User reenrollIdentity(String caUserId, String serverName, ReenrollEntity reenroll) {
        //重新登记证书
        UserAndCerts userCerts = checkUser(caUserId, serverName, true);
        EnrollInfo enrollInfo = new EnrollInfo();
        enrollInfo.setIdentityId(caUserId);
        enrollInfo.setSecret(userCerts.getSecret());
        enrollInfo.setAffiliation(userCerts.getAffiliation());
        enrollInfo.setExtend(reenroll.getEnrollInfo());

        Certificates certificate = userCerts.getCertificate();
        String certPem = certificate.getCertPem();
        String keyPem = certificate.getKeyPem();
        PrivateKey privateKey;
        try {
            privateKey = new CertFileHelper().pemToPrivateKey(keyPem);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ServiceException("私钥文件读取失败", e);
        }
        X509Enrollment enrollment = new X509Enrollment(privateKey, certPem);
        enrollInfo.setEnrollment(enrollment);

        User user;
        try {
            user = requester.reenroll(enrollInfo, createClient(serverName));
        } catch (EnrollmentException e) {
            LOGGER.error(e);
            throw new ServiceException("用户证书登记失败", e);
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("登记用户证书请求参数异常", e);
        }

        try {
            //TODO 替换网络上的旧证书，其实原有证书和新的证书一样有效，fabric通过证书中的公钥来寻找匹配的私钥。新旧证书中公钥一样，所以没必要注销原有旧证书

        } finally {
            //删除本地旧证书，保存新的证书
            certService.deleteCertByPrimaryKey(certificate.getSerialNumber(), certificate.getAuthorityKeyIdentifier());
            saveCert(user, caUserId, serverName);
        }

        if (FabricCaUser.STATE_REGISTERED.equals(userCerts.getState())) {
            fabricCaUserService.updateUserState(caUserId, serverName, FabricCaUser.STATE_ENROLLED);
        }
        return user;
    }

    /**
     * 检查用户信息，并注销该用户原有证书
     */
    private UserAndCerts checkUser(String caUserId, String serverName, boolean checkCert) {
        List<UserAndCerts> usersAndCerts = fabricCaUserService.selectUserCerts(caUserId, serverName);
        if (usersAndCerts == null || usersAndCerts.isEmpty()) {
            throw new ServiceException("找不到指定CA用户(" + serverName + ":" + caUserId + ")的相关信息,请确认该用户是否存在");
        }
        UserAndCerts user = usersAndCerts.get(0);

        if (FabricCaUser.STATE_REVOKED.equals(user.getState())) {
            throw new ServiceException("用户（" + serverName + ":" + caUserId + ")是一个无效的用户");
        }

        if (checkCert) {
            Certificates certificate = user.getCertificate();
            if (certificate == null || StringUtils.isEmpty(certificate.getCertPem()) || StringUtils.isEmpty(certificate.getKeyPem())) {
                throw new ServiceException("CA用户（" + serverName + ":" + caUserId + ")证书缺失，请先登记证书");
            } else {
                if (Certificates.STATE_GOOD.equals(certificate.getState())) {
                    Date notAfter = certificate.getNotAfter();
                    if (notAfter != null && notAfter.getTime() < System.currentTimeMillis()) {
                        throw new ServiceException("CA用户（" + serverName + ":" + caUserId + ")证书已过期");
                    }
                } else {
                    throw new ServiceException("CA用户（" + serverName + ":" + caUserId + ")证书已失效");
                }
            }
        }
        return user;
    }

    private void saveCert(User user, String caUserId, String serverName) {
        Certificates certificates = new Certificates();
        CertFileHelper helper = new CertFileHelper();
        Enrollment enrollment = user.getEnrollment();

        String cert = enrollment.getCert();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
            certificates.setSerialNumber(DatatypeConverter.printHexBinary(x509Cert.getSerialNumber().toByteArray()));
            certificates.setAuthorityKeyIdentifier(helper.getAuthorityKeyIdentifierString(cert));
            certificates.setNotBefore(x509Cert.getNotBefore());
            certificates.setNotAfter(x509Cert.getNotAfter());
        } catch (CertificateException e) {
            LOGGER.error(e);
            throw new ServiceException("X509证书读取失败", e);
        }

        certificates.setCertPem(cert);
        PrivateKey key = enrollment.getKey();
        try {
            certificates.setKeyPem(helper.privateKeyToPem(key));
        } catch (IOException e) {
            throw new ServiceException("私钥读取失败", e);
        }
        certificates.setCaUserId(caUserId);
        certificates.setServerName(serverName);
        certificates.setState(Certificates.STATE_GOOD);
        certService.insertCert(certificates);
    }

    public String revokeIdentity(String caUserId, String serverName, RevokeInfo revokeInfo, String requester) {
        //检查这个用户是否允许被注销
        FabricCaUser user = checkUser(revokeInfo.getRevokee(), serverName, false);
        if (!userService.isSelfOrChild(requester, user.getOwner())) {       //被注销用户是否能被请求者注销掉
            throw new ServiceException("您无权管理" + user.getUserId() + "用户（或其证书）");
        }
        revokeCheck(user, RevokeInfo.RevokeType.IDENTITY);
        return revoke(caUserId, serverName, revokeInfo);
    }

    public String revokeCert(String caUserId, String serverName, RevokeInfo revokeInfo, String requester, boolean checkedPermission) {
        //检查这个证书是否允许被注销
        String serial = revokeInfo.getSerial();
        String aki = revokeInfo.getAki();
        Certificates cert = certService.selectByPrimaryKey(serial, aki);
        if (cert == null) {
            throw new ServiceException("不存的在证书，serial:" + serial + ",aki:" + aki);
        }
        FabricCaUser user = checkUser(cert.getCaUserId(), serverName, false);
        if (!checkedPermission && !userService.isSelfOrChild(requester, user.getOwner())) {       //被注销证书是否能被请求者注销掉
            throw new ServiceException("您无权管理" + user.getUserId() + "用户（或其证书）");
        }
        revokeCheck(user, RevokeInfo.RevokeType.CERTIFICATE);
        Date notAfter = cert.getNotAfter();
        if (StringUtils.isEmpty(cert.getCertPem()) || StringUtils.isEmpty(cert.getKeyPem()) ||
                !Certificates.STATE_GOOD.equals(cert.getState()) || (notAfter != null && notAfter.getTime() < System.currentTimeMillis())) {
            //已经过期的证书不需要也不能够到CA上申请注销
            certService.deleteCertByPrimaryKey(serial, aki);
            if (revokeInfo.isGenCRL()) {
                return generateCRL(caUserId, serverName, new GenerateCrlInfo());
            } else {
                return null;
            }
        } else {
            return revoke(caUserId, serverName, revokeInfo);
        }
    }

    private void revokeCheck(FabricCaUser user, RevokeInfo.RevokeType revokeType) {
        String caUserName = user.getUserId();
        String serverName = user.getServerName();
        String userType = user.getUserType();
        if (StringUtils.isEmpty(userType)) throw new ServiceException("CA用户" + caUserName + "类型为空");

        switch (userType) {
            case "client":
                if (RevokeInfo.RevokeType.IDENTITY == revokeType && "admin".equals(user.getIdentityType())) {
                    List<String> adminUsersName = fabricCaUserService.selectCaAdminUser(serverName);
                    if (adminUsersName != null && adminUsersName.size() == 1 && adminUsersName.contains(caUserName)) {
                        throw new ServiceException("CA用户" + caUserName + "是CA服务" + serverName + "的唯一管理员用户，不可被注销");
                    }
                }
                break;
            case "user":
                if ("admin".equals(user.getIdentityType())) {
                    throw new ServiceException("组织管理员用户" + caUserName + "不可以被注销，如需注销此用户请先去除其管理员权限");
                }
                break;
            case "peer":
                List<String> peersName = peerNodeService.selectPeerByCaUser(caUserName, serverName);
                if (peersName != null && !peersName.isEmpty()) {
                    throw new ServiceException("Peer用户" + caUserName + "正在被Peer节点" + StringUtils.join(peersName.toArray(), ",") + "使用，不可注销");
                }
                break;
            case "orderer":
                List<String> orderersName = ordererNodeService.selectOrdererByCaUser(caUserName, serverName);
                if (orderersName != null && !orderersName.isEmpty()) {
                    throw new ServiceException("Orderer用户" + caUserName + "正在被orderer节点" + StringUtils.join(orderersName.toArray(), ",") + "使用，不可注销");
                }
                break;
            default:
                LOGGER.error("未知的CA用户类型：" + userType);
                throw new ServiceException("未知的CA用户类型：" + userType);
        }
    }

    /**
     * 撤销证书、证书登记、身份
     */
    private String revoke(String caUserId, String serverName, RevokeInfo revokeInfo) {
        User fabricUser = getFabricUserById(caUserId, serverName);
        String revoke;
        try {
            revoke = requester.revoke(revokeInfo, fabricUser, createClient(serverName));
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("注销请求参数异常", e);
        } catch (RevocationException e) {
            LOGGER.error(e);
            throw new ServiceException("注销请求失败", e);
        }

        switch (revokeInfo.getRevokeType()) {
            case IDENTITY:
                String revokee = revokeInfo.getRevokee();
                fabricCaUserService.deleteUser(revokee, serverName);
                break;
            case ENROLLMENT:
                Enrollment enrollment = revokeInfo.getEnrollment();
                try {
                    certService.deleteCertByPem(enrollment.getCert());
                } catch (CertificateException e) {
                    LOGGER.error(e);
                    throw new ServiceException("证书读取失败", e);
                }
                break;
            case CERTIFICATE:
                String serial = revokeInfo.getSerial();
                String aki = revokeInfo.getAki();
                certService.deleteCertByPrimaryKey(serial, aki);
                break;
        }
        return revoke;
    }

    /**
     * 生成证书吊销列表，证书吊销列表的有效期配置在CA的配置文件中(crl.expiry)
     */
    public String generateCRL(String caUserId, String serverName, GenerateCrlInfo crlInfo) {
        User fabricUser = getFabricUserById(caUserId, serverName);
        try {
            return requester.generateCRL(createClient(serverName), fabricUser, crlInfo.getRevokedBefore(), crlInfo.getRevokedAfter(),
                    crlInfo.getExpireBefore(), crlInfo.getExpireAfter());
        } catch (GenerateCRLException e) {
            LOGGER.error(e);
            throw new ServiceException("证书吊销列表请求失败", e);
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("证书吊销列表请求参数异常", e);
        }
    }

    /**
     * 从CA服务端获取满足条件的证书文件
     */
    public ArrayList<String> getHFCACertificates(String caUserId, String serverName, GetCertificatesInfo certificatesInfo) {

        User fabricUser = getFabricUserById(caUserId, serverName);
        HFCACertificateResponse hfcaCertificates;
        try {
            hfcaCertificates = requester.getHFCACertificates(createClient(serverName), fabricUser, certificatesInfo);
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("获取证书请求参数异常", e);
        } catch (HFCACertificateException e) {
            LOGGER.error(e);
            throw new ServiceException("从CA节点上获取证书失败", e);
        }

        int statusCode = hfcaCertificates.getStatusCode();
        if (statusCode != 200) {
            throw new ServiceException("Request CA Failed.");
        }

        ArrayList<HFCACredential> certs = (ArrayList<HFCACredential>) hfcaCertificates.getCerts();


        ArrayList<String> certsPem = new ArrayList<>();
        for (HFCACredential cert : certs) {
            HFCAX509Certificate HFCAX509Cert = (HFCAX509Certificate) cert;
            certsPem.add(HFCAX509Cert.getPEM());
        }
        return certsPem;
    }

    /**
     * 获取CA服务端信息
     */
    public HFCAInfo getCaInfo(String serverName) {
        try {
            return requester.info(createClient(serverName));
        } catch (InvalidArgumentException e) {
            LOGGER.error(e);
            throw new ServiceException("获取CA信息的请求参数异常", e);
        } catch (InfoException e) {
            LOGGER.error(e);
            throw new ServiceException("获取CA服务信息失败", e);
        }
    }


    /**
     * 创建一个HFCAClient对象，用于请求fabric ca server,请求默认的ca实例
     *
     * @param serverName ca server 名称
     * @return HFCAClient对象
     */
    private HFCAClient createClient(String serverName) {
        return createClient(serverName, null);
    }

    /**
     * 创建一个HFCAClient对象，用于请求fabric ca server
     *
     * @param serverName ca server 名称
     * @param caName     ca实例名称，用于指定具体的ca实例，如果空置则使用默认实例
     * @return HFCAClient对象
     */
    private HFCAClient createClient(String serverName, String caName) {
        FabricCaServer server;
        try {
            server = serverService.getServer(serverName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + serverName + "的CA服务");
        }
        /*
          根据serverName获取URL和TLS证书
          url          fabric ca server服务地址，<proto>://<ip>:<port>
          properties   请求fabric ca的通信加密证书，如果通信协议是https这个参数就不能为空
         */
        String hostIp;
        try {
            hostIp = hostService.getHost(server.getHostName()).getIp();
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + server.getHostName() + "的主机");
        }
        String url;
        Properties properties = null;//caInfo.getProperties();
        if (server.getTlsEnable()) {
            url = "https://" + hostIp + ":" + server.getExposedPort();

            //设置tls证书
//            String tlsCert = server.getTlsCert();
//            if (tlsCert == null || tlsCert.isEmpty()) {
//                throw new NullPointerException("Proto is https, but properties is null.");
//            }
            properties = new Properties();
//            properties.put("pemBytes", tlsCert.getBytes());
//            properties.setProperty("allowAllHostNames", "true"); //true or false
        } else {
            url = "http://" + hostIp + ":" + server.getExposedPort();
        }

        try {
            URL purl = new URL(url);
            final String proto = purl.getProtocol();
            HFCAClient client;
            if ("http".equals(proto)) {
                if (caName == null || caName.isEmpty()) {
                    client = HFCAClient.createNewInstance(url, null);
                } else {
                    client = HFCAClient.createNewInstance(caName, url, null);
                }
            } else if ("https".equals(proto)) {
                if (properties == null || properties.isEmpty()) {
                    throw new NullPointerException("Proto is https, but properties is null.");
                }

                if (caName == null || caName.isEmpty()) {
                    client = HFCAClient.createNewInstance(url, properties);
                } else {
                    client = HFCAClient.createNewInstance(caName, url, properties);
                }
            } else {
                throw new IllegalArgumentException("HFCAClient only supports http or https not " + proto);
            }
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            client.setCryptoSuite(cryptoSuite);
            return client;
        } catch (Exception e) {
            LOGGER.warn(e);
            throw new ServiceException("Fabric ca请求客户端对象创建失败:" + e.getMessage());
        }
    }
}
