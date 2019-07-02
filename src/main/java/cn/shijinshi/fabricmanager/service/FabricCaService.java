package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.dao.CertificatesService;
import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.ZipFileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabric ca本地数据（数据库）相关的服务
 */
@Service
public class FabricCaService {
    private static final Logger LOGGER = Logger.getLogger(FabricCaService.class);
    @Autowired
    private FabricCaServerService caServerService;
    @Autowired
    private FabricCaUserService caUserService;
    @Autowired
    private CertificatesService certService;
    @Autowired
    private UserService userService;


    public List<Map<String, Object>> getCaServers() {
        List<Map<String, Object>> servers = new ArrayList<>();
        List<FabricCaServer> fabricCaServers = caServerService.selectAllServer();
        if (fabricCaServers != null && !fabricCaServers.isEmpty()) {
            for (FabricCaServer fabricCaServer : fabricCaServers) {
                Map<String, Object> server = new HashMap<>();
                server.put("serverName", fabricCaServer.getServerName());
                server.put("hostName", fabricCaServer.getHostName());
                server.put("containerId", fabricCaServer.getContainerId());
                server.put("creator", fabricCaServer.getCreator());
                server.put("port", fabricCaServer.getPort());
                server.put("exposedPort", fabricCaServer.getExposedPort());
                server.put("home", fabricCaServer.getHome());
                server.put("affiliations", fabricCaServer.getAffiliations());
                servers.add(server);
            }
        }
        return servers;
    }

    public List<Map<String, Object>> getCaUsers(String userId) {
        List<UserAndCerts> fabricCaUsers = caUserService.selectByRequester(userId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (UserAndCerts caUser : fabricCaUsers) {
            Map<String, Object> mate = new HashMap<>();
            mate.put("userId", caUser.getUserId());
            mate.put("serverName", caUser.getServerName());
            mate.put("owner", caUser.getOwner());
            mate.put("type", caUser.getUserType());
            mate.put("affiliation", caUser.getAffiliation());
            mate.put("attributes", caUser.getAttributes());
            mate.put("maxEnrollments", caUser.getMaxEnrollments());

            Certificates certificate = caUser.getCertificate();
            if (certificate != null) {
                mate.put("notBefore", certificate.getNotBefore());
                mate.put("notAfter", certificate.getNotAfter());
            } else {
                mate.put("notBefore", null);
                mate.put("notAfter", null);
            }
           data.add(mate);
        }
        return data;
    }

    public List<Certificates> getCertsInfoByUser(String caUserId, String serverName, String requester) {
        List<Certificates> certs = getCertsByUser(caUserId, serverName, requester);
        List<Certificates> certList = new ArrayList<>();
        for (Certificates cert: certs) {
            Certificates certificate = new Certificates();
            certificate.setSerialNumber(cert.getSerialNumber());
            certificate.setAuthorityKeyIdentifier(cert.getAuthorityKeyIdentifier());
            certificate.setCaUserId(cert.getCaUserId());
            certificate.setServerName(cert.getServerName());
            certificate.setNotBefore(cert.getNotBefore());
            certificate.setNotAfter(cert.getNotAfter());
            certList.add(certificate);
        }
        return certList;
    }

    private List<Certificates> getCertsByUser(String caUserId, String serverName, String requester) {
        if (checkPermission(requester, caUserId, serverName)) {
            List<Certificates> certs = certService.getCertByUser(caUserId, serverName);
            if (certs == null || certs.isEmpty()) {
                throw new ServiceException("系统中不存在CA用户" + caUserId + "的证书和私钥，请先登记该用户的证书");
            }
            return certs;
        } else {
            throw new ServiceException("无权限获取CA用户" + serverName +":"+caUserId+"证书");
        }
    }

    private FabricCaUser getCaUser(String serverName, String caUserId, String requester) {
        if (checkPermission(requester, caUserId, serverName)) {
            try {
                return caUserService.getCaUser(caUserId, serverName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("系统中不存在CA用户" + caUserId);
            }
        }else {
            throw new ServiceException("无权限获取CA用户" + serverName +":"+caUserId+"证书");
        }
    }

    public File downloadCertByUser(String caUserId, String serverName, String requester) {

        List<Certificates> certs = getCertsByUser(caUserId, serverName, requester);
        FabricCaUser caUser = getCaUser(serverName, caUserId, requester);

        CertFileHelper certHelper = new CertFileHelper();

        String packageName = serverName + "-" + caUserId + "-certAndKey";
        String savePathTemp = ExternalResources.getTemp(packageName);
        //保存文件
        try {
            if (caUser.getTlsEnable()) {
                String tlsTemp = savePathTemp + File.separator + "tls";
                FileUtils.writeStringToFile(tlsTemp, caUser.getTlsCert(), "tlscert.pem");
                String keyFileName = certHelper.getFabricPrivateKeyName(caUser.getTlsCert());
                FileUtils.writeStringToFile(tlsTemp, caUser.getTlsKey(), keyFileName);
            }
            for (Certificates cert : certs) {
                String certTemp = savePathTemp + File.separator + cert.getSerialNumber();
                FileUtils.writeStringToFile(certTemp, cert.getCertPem(), "cert.pem");
                String keyFileName = certHelper.getFabricPrivateKeyName(cert.getCertPem());
                FileUtils.writeStringToFile(certTemp, cert.getKeyPem(), keyFileName);
            }
        }catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("证书文件导出失败", e);
        }
        //打包文件
        ZipFileUtils zipFileUtils = new ZipFileUtils();
        String zipFile = null;
        try {
            zipFile = zipFileUtils.createZip(savePathTemp, ExternalResources.getTemp(packageName + ".zip"));
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ServiceException("文件打包失败", e);
        }
        //删除临时文件
        FileUtils.delete(savePathTemp);
        return new File(zipFile);
    }

    public File downloadCertBySerial(String serial, String aki, String requester) {
        Certificates cert = certService.selectByPrimaryKey(serial, aki);
        if (cert == null) {
            throw new ServiceException("不存在证书与serial:" + serial + ",aki:" + aki);
        }

        String caUserId = cert.getCaUserId();
        String serverName = cert.getServerName();
        if (checkPermission(requester, caUserId, serverName)) {
            CertFileHelper helper = new CertFileHelper();

            String packageName = null;
            try {
                packageName = helper.packCertAndKey(cert.getCertPem(), cert.getKeyPem(), caUserId + "@" + serverName + ".pem");
            } catch (Exception e) {
                LOGGER.error(e);
                throw new ServiceException("文件打包失败", e);
            }
            return new File(packageName);
        } else {
            throw new ServiceException("无权限获取证书serial:" + serial + ",aki:" + aki);
        }
    }

    public boolean checkPermission(String requester, String caUserId, String serverName) {
        FabricCaUser caUser;
        try {
            caUser = caUserService.getCaUser(caUserId, serverName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("未知的CA用户" + serverName + ":" + caUserId);
        }

        String owner = caUser.getOwner();
        return userService.isSelfOrChild(requester, owner);
    }
}

