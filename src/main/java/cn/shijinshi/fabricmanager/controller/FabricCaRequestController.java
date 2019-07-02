package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.annotation.CaUserPermissionVerify;
import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.fabricca.request.*;
import cn.shijinshi.fabricmanager.exception.DownloadFileException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.FabricCaRequestService;
import cn.shijinshi.fabricmanager.service.TokenManager;
import cn.shijinshi.fabricmanager.service.ca.entity.GenerateCrlInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.GetCertificatesInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.RegisterInfo;
import cn.shijinshi.fabricmanager.service.ca.entity.RevokeInfo;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/ca")
public class FabricCaRequestController {
    private final FabricCaRequestService requester;
    private final TokenManager tokenManager;

    @Autowired
    public FabricCaRequestController( FabricCaRequestService requester, TokenManager tokenManager) {
        this.requester = requester;
        this.tokenManager = tokenManager;
    }

    //--------------------------------------------------- fabric ca server ---------------------------------------------

    /**
     * 通过fabric ca server的REST接口获取fabric ca的基本信息
     */
    @GetMapping("/server/{serverName}")
    public Response getCaServer(@PathVariable("serverName") String serverName) {
        HFCAInfo caInfo = requester.getCaInfo(serverName);
        return new Response().success(caInfo);
    }

    //-------------------------------------------- fabric ca user ------------------------------------------------------
    /**
     * 在CA上注册一个用户，并登记这个用户的证书
     */
    @CaUserPermissionVerify
    @PostMapping("/user/register/{serverName}/{caUserId}")
    public Response registerCaUser(@RequestBody @Valid RegisterCaUserEntity register, HttpServletRequest request,
                                   @PathVariable("serverName") String serverName, @PathVariable("caUserId") String caUserId) {
        String creator = tokenManager.getRequester(request).getUserId();

        //注册用户
        RegisterInfo registerInfo = register.getRegisterInfo();
        requester.registerIdentity(serverName, creator, caUserId, registerInfo);

        //登记用户证书
        requester.enrollIdentity(registerInfo.getIdentityId(), serverName);
        return new Response().success("CA用户" + registerInfo.getIdentityId() + "注册成功");
    }

    /**
     * 登记用户证书
     * 本地生成证书和私钥，通过这对公私钥生成CSR（Certificate Signing Request）发送给CA服务端签发一个证书到指定用户下
     */
    @CaUserPermissionVerify
    @PostMapping(value = "/user/enroll/{serverName}/{caUserId}")
    public Object enrollCaUser(@RequestBody EnrollEntity enroll, @PathVariable("serverName") String serverName,
                               @PathVariable("caUserId") String caUserId) throws DownloadFileException {

        User user = requester.enrollIdentity(caUserId, serverName, enroll);
        if (enroll.getDownload()) {
            return downloadEnrollment(user.getEnrollment());
        } else {
            return new Response().success("用户" + serverName + ":" + caUserId + "登记成功.");
        }
    }

    /**
     * 重新登记用户证书
     * 本接口需要指定一个CA用户和此用户一对证书和私钥。通过证书中的公钥和私钥生成新的CSR（Certificate Signing Request）
     * 发送给CA服务端签发一个证书到指定用户名下。原有证书和私钥任然有效
     */
    @CaUserPermissionVerify
    @PostMapping(value = "/user/reenroll/{serverName}/{caUserId}")
    public Object reenrollCaUser(@RequestBody ReenrollEntity reenroll, @PathVariable("serverName") String serverName,
                                 @PathVariable("caUserId") String caUserId) throws DownloadFileException {
        User user = requester.reenrollIdentity(caUserId, serverName, reenroll);
        if (reenroll.getDownload()) {
            return downloadEnrollment(user.getEnrollment());
        } else {
            return new Response().success("用户" + serverName + ":" + caUserId + "重新登记成功.");
        }
    }

    private static final Logger LOGGER = Logger.getLogger(FabricCaRequestController.class);

    private ResponseEntity<byte[]> downloadEnrollment(Enrollment enrollment) throws DownloadFileException {
        CertFileHelper helper = new CertFileHelper();
        File file;
        try {
            file = new File(helper.packCertAndKey(enrollment));
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ServiceException("证书文件打包失败", e);
        }
        ResponseEntity<byte[]> responseEntity = DownloadHelper.getResponseEntity(file);
        if (!file.delete()) {
            LOGGER.warn("Temp file " + file.getPath() + " failed to delete");
        }
        return responseEntity;
    }

    /**
     * 在CA上注销指定CA用户
     */
    @CaUserPermissionVerify
    @DeleteMapping("/user/revoke/{serverName}/{caUserId}")
    public Response revokeUser(@RequestBody @Valid RevokeUserEntity body, HttpServletRequest request,
                               @PathVariable("serverName") String serverName, @PathVariable("caUserId") String caUserId) {

        String userId = tokenManager.getRequester(request).getUserId();
        String reason = body.getReason();
        boolean genCRL = body.isGenCRL();
        RevokeInfo revokeInfo = new RevokeInfo(RevokeInfo.Reason.fromString(reason), genCRL, body.getRevokee());
        String crl = requester.revokeIdentity(caUserId, serverName, revokeInfo, userId);
        Map<String, String> data = new HashMap<>();
        data.put("crl", crl);
        return new Response().success("CA user revoke success.");
    }


    //----------------------------------------- fabric ca cert ---------------------------------------------------------

    /**
     * 在CA上注销指定证书
     */
    @CaUserPermissionVerify
    @DeleteMapping("/cert/revoke/{serverName}/{caUserId}")
    public Response revokeCert(@RequestBody @Valid RevokeCertEntity body, HttpServletRequest request,
                               @PathVariable("serverName") String serverName,
                               @PathVariable("caUserId") String caUserId) {
        String aki = body.getAki();
        String serial = body.getSerial();
        String reason = body.getReason();
        boolean genCRL = body.isGenCRL();
        RevokeInfo revokeInfo = new RevokeInfo(RevokeInfo.Reason.fromString(reason), genCRL, serial, aki);
        String userId = tokenManager.getRequester(request).getUserId();
        String crl = requester.revokeCert(caUserId, serverName, revokeInfo, userId, false);
        Map<String, String> data = new HashMap<>();
        data.put("crl", crl);
        return new Response().success(data);
    }

    /**
     * 请求CA生成证书吊销列表
     */
    @CaUserPermissionVerify
    @PostMapping("/cert/crl/{serverName}/{caUserId}")
    public Response generateCRL(@RequestBody @Valid GenerateCrlEntity body, @PathVariable("serverName") String serverName,
                                @PathVariable("caUserId") String caUserId) {
        GenerateCrlInfo generateCrlInfo = body.getCrlInfo();
        if (generateCrlInfo == null) {
            generateCrlInfo = new GenerateCrlInfo();
        }
        String crl = requester.generateCRL(caUserId, serverName, generateCrlInfo);
        HashMap<String, String> data = new HashMap<>();
        data.put("crl", crl);
        return new Response().success(data);
    }


    /**
     * 从CA上获取符合条件的证书
     */
    @CaUserPermissionVerify
    @PostMapping("/cert/{serverName}/{caUserId}")
    public Response getCertFromCa(@RequestBody @Valid GetCertFromCaEntity body, @PathVariable("serverName") String serverName,
                                  @PathVariable("caUserId") String caUserId) {
        GetCertificatesInfo certInfo = body.getCertInfo();
        ArrayList<String> certs = requester.getHFCACertificates(caUserId, serverName, certInfo);
        return new Response().success(certs);
    }
}
