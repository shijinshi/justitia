package cn.shijinshi.fabricmanager.service.ca;

import cn.shijinshi.fabricmanager.service.fabric.FabricUserImpl;
import cn.shijinshi.fabricmanager.service.ca.entity.EnrollInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.GetCertificatesInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.RegisterInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.RevokeInfo;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.*;
import org.hyperledger.fabric_ca.sdk.exception.*;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Service
public class FabricCaRequester {
    /**
     * 在fabric ca server上注册一个身份（identity）
     * @param registerInfo 注册身份需要的配置信息
     * @return
     * @throws Exception
     */
    public String register(RegisterInfo registerInfo, User parentUser, HFCAClient client) throws RegistrationException, InvalidArgumentException {
        String identityName = registerInfo.getIdentityId();
        String secret = registerInfo.getSecret();
        String affiliation = registerInfo.getAffiliation();
        if (affiliation == null) {
            affiliation = parentUser.getAffiliation();   //和父用户同级
        }
        /*
           Because Fabric CA uses Viper to read configuration file. Viper treats map keys as case insensitive and always returns lowercase value.
           The affiliation of the registrar must be equal to or a prefix of the affiliation of the identity being registered.

           Fields	                Required	    Default Value
           ID	                    Yes
           Secret	                No
           Affiliation         	    No	            parentUser’s Affiliation
           Type	                    No	            client
           Maxenrollments	        No	            0
           Attributes	            No
         */
        RegistrationRequest request;
        try {
            request = new RegistrationRequest(identityName, affiliation.toLowerCase());
        } catch (Exception e) {
            throw new InvalidArgumentException(e.getMessage());
        }
        request.setSecret(secret);
        request.setType(registerInfo.getUserType());
        /*
          Setting maxenrollments to -1 or leaving it out from the configuration will result in the identity being registered to use the CA’s max enrollment value.
          Furthermore, the max enrollment value for an identity being registered cannot exceed the CA’s max enrollment value.
          For example, if the CA’s max enrollment value is 5. Any new identity must have a value less than or equal to 5,
          and also can’t set it to -1 (infinite enrollments).
         */
        request.setMaxEnrollments(registerInfo.getMaxEnrollments());
        /*
          fabric ca register可以使用的系统属性
                   name                    type                            Description
            hf.Registrar.Roles	            List	    List of roles that the registrar is allowed to manage
            hf.Registrar.DelegateRoles	    List	    List of roles that the registrar is allowed to give to a registree for its ‘hf.Registrar.Roles’ attribute
            hf.Registrar.Attributes	    List	    List of attributes that registrar is allowed to register
            hf.GenCRL	                    Boolean	    Identity is able to generate CRL if attribute value is true
            hf.Revoker	                    Boolean	    Identity is able to revoke a identity and/or certificates if attribute value is true
            hf.AffiliationMgr	            Boolean	    Identity is able to manage affiliations if attribute value is true
            hf.IntermediateCA	            Boolean	    Identity is able to enroll as an intermediate CA if attribute value is true
            使用规则：
               1、对于以上boolean类型的属性，如果注册者为false则被注册者不能为true，不设置则默认为false
               2、被注册者的hf.Registrar.Roles取值必须是注册者的子集
               3、如果某个键的值被多次指定，则以最后一次为准（自定义的键值也一样）
         */
        Map<String, Object> attributes = registerInfo.getAttributes();
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            request.addAttribute(new Attribute(attribute.getKey(), attribute.getValue().toString()));
        }

        return client.register(request, parentUser);
    }

    /**
     * 假设您的注册证书即将过期或已被盗用。 您可以发出reenroll命令以续订注册证书。
     * @param enrollInfo 登记身份需要的配置信息
     * @return
     * @throws Exception
     */
    public User reenroll(EnrollInfo enrollInfo, HFCAClient client) throws EnrollmentException, InvalidArgumentException {
        return enroll(enrollInfo, client,true);
    }

    /**
     * 登记一个用户，fabric ca server会生成这个用户的pem证书，证书中默认包含EnrollmentID、Type和Affiliation三个属性。
     * @param enrollInfo 登记身份需要的配置信息
     * @return
     * @throws Exception
     */
    public User enroll(EnrollInfo enrollInfo, HFCAClient client) throws EnrollmentException, InvalidArgumentException {
        return enroll(enrollInfo, client,false);
    }

    private User enroll(EnrollInfo enrollInfo, HFCAClient client, boolean reenroll) throws InvalidArgumentException, EnrollmentException {
        EnrollmentRequest request = new EnrollmentRequest();
        String host = enrollInfo.getHost();
        if (host != null && !host.isEmpty()) {
            request.addHost(enrollInfo.getHost());
        }
        /*
          这个参数不指定，我们使用fabric ca server配置中指定的证书生成算法。这样可以保证我们的证书算法一致
          String csr = enrollIdentity.getCsr();
          if (csr != null && !"".equals(csr)) helper.setCsr(csr);
         */
        KeyPair keypair = enrollInfo.getKeypair();
        if (keypair != null) request.setKeyPair(keypair);
        String label = enrollInfo.getLabel();
        if (label != null && !"".equals(label)) request.setLabel(label);
        String profile = enrollInfo.getProfile();
        if (profile != null && !"".equals(profile)) request.setProfile(profile);

        ArrayList<String> attrReq = enrollInfo.getAttrReq();
        if (attrReq != null) {
            for (String attr : attrReq) {
                request.addAttrReq(attr);
            }
        }

        String IdentityId = enrollInfo.getIdentityId();
        String secret = enrollInfo.getSecret();
        String affiliation = enrollInfo.getAffiliation();
        FabricUserImpl user = new FabricUserImpl(IdentityId, affiliation);
        Enrollment enroll;
        if(reenroll) {
            user.setEnrollment(enrollInfo.getEnrollment());
            enroll = client.reenroll(user, request);
        } else {
            enroll = client.enroll(IdentityId, secret, request);
        }
        user.setEnrollment(enroll);
        return user;
    }

    /**
     * 吊销身份将撤销身份所拥有的所有证书，并且还将阻止此身份获取任何新证书。
     * 吊销证书将使单个证书无效。
     * In order to revoke a certificate or an identity, the calling identity must have the hf.Revoker and hf.Registrar.Roles attribute.
     * The revoking identity can only revoke a certificate or an identity that has an affiliation that is equal to or prefixed by the revoking identity’s affiliation.
     * Furthermore, the revoker can only revoke identities with types that are listed in the revoker’s hf.Registrar.Roles attribute.
     *
     * For example, a revoker with affiliation orgs.org1 and ‘hf.Registrar.Roles=peer,client’ attribute can revoke either a peer or client type
     * identity affiliated with orgs.org1 or orgs.org1.department1 but can’t revoke an identity affiliated with orgs.org2 or of any other type.
     *
     * An enrollment certificate that belongs to an identity can be revoked by specifying its AKI (Authority Key Identifier) and serial number.
     * For example, you can get the AKI and the serial number of a certificate using the openssl command and pass them to the revoke command
     * to revoke the said certificate as follows:
     *      serial=$(openssl x509 -in userecert.pem -serial -noout | cut -d "=" -f 2)
     *      aki=$(openssl x509 -in userecert.pem -text | awk '/keyid/ {gsub(/ *keyid:|:/,"",$1);print tolower($0)}')
     *
     *
     *
     * Note:
     *      After a certificate is revoked in the Fabric CA server, the appropriate MSPs in Hyperledger Fabric must also be updated.
     *      This includes both local MSPs of the peers as well as MSPs in the appropriate channel configuration blocks.
     */
    public String revoke(RevokeInfo revokeInfo, User revoker, HFCAClient client) throws InvalidArgumentException, RevocationException {
        switch (revokeInfo.getRevokeType()) {
            case IDENTITY:
                return client.revoke(revoker, revokeInfo.getRevokee(), revokeInfo.getReason(), revokeInfo.isGenCRL());
            case ENROLLMENT:
                return client.revoke(revoker, revokeInfo.getEnrollment(), revokeInfo.getReason(), revokeInfo.isGenCRL());
            case CERTIFICATE:
                return client.revoke(revoker, revokeInfo.getSerial(), revokeInfo.getAki(), revokeInfo.getReason(), revokeInfo.isGenCRL());
            default:
                return null;
        }
    }

    /**
     * 过期（expire）和撤销（revoked）是两种不同的状态，过期了的证书不会自动撤销。过期后的证书可以reenroll，但revoke以后就不可了
     *
     * @param registrar     admin user configured in CA-server
     * @param revokedBefore Restrict certificates returned to revoked before this date if not null.
     * @param revokedAfter  Restrict certificates returned to revoked after this date if not null.
     * @param expireBefore  Restrict certificates returned to expired before this date if not null.
     * @param expireAfter   Restrict certificates returned to expired after this date if not null.
     * @return
     * @throws Exception
     */
    public String generateCRL( HFCAClient client, User registrar, Date revokedBefore, Date revokedAfter, Date expireBefore, Date expireAfter)
            throws GenerateCRLException, InvalidArgumentException {
        return client.generateCRL(registrar, revokedBefore, revokedAfter, expireBefore, expireAfter);
    }

    /**
     * Gets all certificates that the registrar is allowed to see and based on filter parameters that
     * are part of the certificate helper.
     *
     * @param registrar The identity of the registrar (i.e. who is performing the registration).
     * @param certificatesInfo The certificate helper that contains filter parameters
     * @return HFCACertificateResponse object
     * @return
     * @throws Exception
     */
    public HFCACertificateResponse getHFCACertificates( HFCAClient client, User registrar, GetCertificatesInfo certificatesInfo)
            throws InvalidArgumentException, HFCACertificateException {

        HFCACertificateRequest req = client.newHFCACertificateRequest();

        String enrollmentID = certificatesInfo.getEnrollmentId();
        if (enrollmentID != null && !enrollmentID.isEmpty()) req.setEnrollmentID(enrollmentID);

        String serial = certificatesInfo.getSerial();
        if (serial != null && !serial.isEmpty()) req.setSerial(serial);

        String aki = certificatesInfo.getAki();
        if (aki != null && !aki.isEmpty()) req.setAki(aki);

        boolean revoked = certificatesInfo.isRevoked();
        if (revoked) req.setRevoked(revoked);

        Date revokedStart = certificatesInfo.getRevokedStart();
        if (revokedStart != null) req.setRevokedStart(revokedStart);

        Date revokedEnd = certificatesInfo.getRevokedEnd();
        if (revokedEnd != null) req.setRevokedEnd(revokedEnd);

        boolean expired = certificatesInfo.isExpired();
        if (expired) req.setExpired(expired);

        Date expiredStart = certificatesInfo.getExpiredStart();
        if (expiredStart != null) req.setExpiredStart(expiredStart);

        Date expiredEnd = certificatesInfo.getExpiredEnd();
        if (expiredEnd != null) req.setExpiredEnd(expiredEnd);

        return client.getHFCACertificates(registrar, req);
    }

    public HFCAInfo info( HFCAClient client) throws InvalidArgumentException, InfoException {
        return client.info();
    }

}
