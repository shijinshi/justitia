package cn.shijinshi.fabricmanager.service.fabric.certificate;

import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.FabricCaManageService;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

@Service
public class MspHelper {
    private final FabricCaManageService caManageService;
    private final FabricCaServerService caServerService;
    private final FabricCaUserService caUserService;

    @Autowired
    public MspHelper(FabricCaManageService caManageService, FabricCaServerService caServerService, FabricCaUserService caUserService) {
        this.caManageService = caManageService;
        this.caServerService = caServerService;
        this.caUserService = caUserService;
    }


    /**
     * 构建节点MSP目录
     *
     * @param tlsEnable       是否使用TLS
     * @param tlsCertPem      TLS证书
     * @param tlsPriKeyPem    TLS私钥
     * @param tlsCaServerName TLS ca服务
     * @param signCertPem     节点签名证书
     * @param signPriKeyPem   节点签名私钥
     * @return 构建好的MSP临时存放目录
     * @throws IOException              文件写入失败
     * @throws CertificateException     证书文件解析是失败
     * @throws NoSuchAlgorithmException 证书文件解析失败
     */
    public String generateNodeMsp(boolean tlsEnable, String tlsCertPem, String tlsPriKeyPem, String tlsCaServerName,
                                  String signCertPem, String signPriKeyPem)
            throws IOException, CertificateException, NoSuchAlgorithmException {

        String tempDir = ExternalResources.getUniqueTempDir();

        try {
            //msp
            String mspDir = tempDir + File.separator + "msp";
            writeNodeMsp(mspDir, tlsEnable, signCertPem, signPriKeyPem, tlsCaServerName);
            String tlsCaCert = mspDir + File.separator + "tlscacerts" + File.separator + tlsCaServerName + "-cert.pem";
            //tls
            if (tlsEnable) {
                String localTls = tempDir + File.separator + "tls";
                FileUtils.writeStringToFile(localTls, tlsCertPem, "server.crt");
                FileUtils.writeStringToFile(localTls, tlsPriKeyPem, "server.key");
                FileUtils.copyFile(new File(tlsCaCert), new File(localTls + File.separator + "ca.crt"));
            }
        } catch (Exception e) {
            FileUtils.delete(tempDir);
            throw e;
        }
        return tempDir;
    }

    private void writeNodeMsp(String mspDir, boolean tlsEnable, String signCertPem, String signPriKeyPem, String tlsCaServerName)
            throws IOException, CertificateException, NoSuchAlgorithmException {

        writeAdminCerts(mspDir);
        writeCaCerts(mspDir);
        writeKeyStore(mspDir, signCertPem, signPriKeyPem);
        writeSignCerts(mspDir, signCertPem);
        writeTlsCaCerts(mspDir, tlsEnable, tlsCaServerName);
    }

    public String generateOrgMsp(boolean tlsEnable, String tlsCaServerName) throws IOException {
        String mspDir = ExternalResources.getUniqueTempDir();
        try {
            writeOrgMsp(mspDir, tlsEnable, tlsCaServerName);
        } catch (Exception e) {
            FileUtils.delete(mspDir);
            throw e;
        }
        return mspDir;
    }

    private void writeOrgMsp(String mspDir, boolean tlsEnable, String tlsCaServerName) throws IOException {
        writeAdminCerts(mspDir);
        writeCaCerts(mspDir);
        writeTlsCaCerts(mspDir, tlsEnable, tlsCaServerName);
    }

    private void writeAdminCerts(String mspDir) throws IOException {
        //获取组织的admin用户证书到这个目录下
        String admincertsPath = mspDir + File.separator + "admincerts";
        FileUtils.makeDir(admincertsPath);
        List<UserAndCerts> userAndCerts = caUserService.selectOrgAdminUser();
        if (userAndCerts != null && !userAndCerts.isEmpty()) {
            for (UserAndCerts user : userAndCerts) {
                Certificates certificate = user.getCertificate();
                if (certificate != null && StringUtils.isNotEmpty(certificate.getCertPem())) {
                    String certFileName = user.getUserId() + "@" + user.getServerName() + "-cert.pem";
                    FileUtils.writeStringToFile(admincertsPath, certificate.getCertPem(), certFileName);
                }
            }
        } else {
            throw new ServiceException("组织至少需要有一个类型为user身份为admin的ca用户才能部署节点");
        }
    }

    private void writeCaCerts(String mspDir) throws IOException {
        //根CA证书
        FabricCaServer rootServer = caServerService.selectRootServer();
        if (rootServer != null) {
            String cacertsPath = mspDir + File.separator + "cacerts";
            FileUtils.makeDir(cacertsPath);
            String serverName = rootServer.getServerName();
            File caCert = caManageService.getCaCert(serverName);
            FileUtils.copyFile(caCert, new File(cacertsPath + File.separator + serverName + "-cert.pem"));
        } else {
            throw new ServiceException("不存在根CA,请先配置根CA");
        }
        //中间CA证书
        List<FabricCaServer> intermediateService = caServerService.selectIntermediateServer();
        if (intermediateService != null) {
            String intermediatecertsPath = mspDir + File.separator + "intermediatecerts";
            for (FabricCaServer intermediateServer : intermediateService) {
                String serverName = intermediateServer.getServerName();
                String temppath = intermediatecertsPath + File.separator + serverName;
                FileUtils.makeDir(temppath);
                File caCert = caManageService.getCaCert(serverName);
                FileUtils.copyFile(caCert, new File(intermediatecertsPath + File.separator + serverName + "-cert.pem"));
                FileUtils.delete(temppath);
            }
        }
    }

    private void writeKeyStore(String mspDir, String signCertPem, String signPriKeyPem) throws CertificateException, NoSuchAlgorithmException, IOException {
        //签名私钥
        CertFileHelper certHelper = new CertFileHelper();
        String keyFileName = certHelper.getFabricPrivateKeyName(signCertPem);
        String keystorePath = mspDir + File.separator + "keystore";
        FileUtils.writeStringToFile(keystorePath, signPriKeyPem, keyFileName);
    }

    private void writeSignCerts(String mspDir, String signCertPem) throws IOException {
        //签名证书
        String siancertsPath = mspDir + File.separator + "signcerts";
        FileUtils.writeStringToFile(siancertsPath, signCertPem, "cert.pem");
    }

    private void writeTlsCaCerts(String mspDir, boolean tlsEnable, String tlsCaServerName) throws IOException {
        if (tlsEnable) {
            if (StringUtils.isNotEmpty(tlsCaServerName)) {
                String localTls = mspDir + File.separator + "tlscacerts";
                FileUtils.makeDir(localTls);
                File caCert = caManageService.getCaCert(tlsCaServerName);
                FileUtils.copyFile(caCert, new File(localTls + File.separator + tlsCaServerName + "-cert.pem"));
            } else {
                throw new ServiceException("启用了TLS配置，但未指定TLS CA服务");
            }
        }
    }
}
