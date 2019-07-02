package cn.shijinshi.fabricmanager.service.fabric.node.orderer;

import cn.shijinshi.fabricmanager.controller.entity.orderer.CreateOrdererEntity;
import cn.shijinshi.fabricmanager.dao.OrdererNodeService;
import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.entity.OrdererNode;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.DockerService;
import cn.shijinshi.fabricmanager.service.fabric.certificate.MspHelper;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrdererManager {
    private static final String FABRIC_CONFIG_PATH = "/home/";
    private static final String MSP_PATH = FABRIC_CONFIG_PATH + "msp";
    private static final String TLS_PATH = FABRIC_CONFIG_PATH + "tls";
    private static final String GENESIS_BLOCK_PATH = FABRIC_CONFIG_PATH + "genesis.block";

    @Autowired
    private DockerService dockerService;

    @Autowired
    private MspHelper mspHelper;
    @Autowired
    private OrdererNodeService ordererService;

    public String createOrderer(CreateOrdererEntity config, Organization org, UserAndCerts ordererUser) {
        //设置Orderer默认配置、根据用户传入参数生成配置
        setDefaultConfig(config, org.getOrgMspId(), ordererUser.getTlsEnable());
        //创建Orderer节点
        CreateContainerResponse response = dockerService.createContainer(config);
        return response.getId();
    }


    private void setDefaultConfig(CreateOrdererEntity config, String mspId, boolean tlsEnable) {
        //设置默认配置
        if (StringUtils.isEmpty(config.getCmd())) {
            config.setCmd("orderer");
        }
        if (StringUtils.isEmpty(config.getWorkingDir())) {
            config.setWorkingDir(FABRIC_CONFIG_PATH);
        }
        if (config.getServerPort() == 0) {
            config.setServerPort(7050);
        }

        //设置默认的环境变量
        Map<String, String> envs = config.getEnv();
        if (envs == null) {
            envs = new HashMap<>();
        }
        envs.put("ORDERER_GENERAL_LOCALMSPID", mspId);

        if (StringUtils.isEmpty(envs.get("ORDERER_GENERAL_LOGLEVEL"))) {
            envs.put("ORDERER_GENERAL_LOGLEVEL", "DEBUG");
        }
        if (StringUtils.isEmpty(envs.get("ORDERER_GENERAL_LISTENADDRESS"))) {
            envs.put("ORDERER_GENERAL_LISTENADDRESS", "0.0.0.0");
        }

        //设置genesisBlock
        envs.put("ORDERER_GENERAL_GENESISMETHOD", "file");
        envs.put("ORDERER_GENERAL_GENESISFILE", GENESIS_BLOCK_PATH);
        //设置MSP
        envs.put("ORDERER_GENERAL_LOCALMSPDIR", MSP_PATH);
        //设置TLS
        if (tlsEnable) {
            envs.put("ORDERER_GENERAL_TLS_ENABLED", "true");
            envs.put("ORDERER_GENERAL_TLS_PRIVATEKEY", TLS_PATH + "/server.key");
            envs.put("ORDERER_GENERAL_TLS_CERTIFICATE", TLS_PATH + "/server.crt");
            envs.put("ORDERER_GENERAL_TLS_ROOTCAS", "[" + TLS_PATH + "/ca.crt" + "]");
        } else {
            envs.put("ORDERER_GENERAL_TLS_ENABLED", "false");
        }
    }

    public void sendMspToContainer(String containerId, String hostName, UserAndCerts ordererUser, String tlsCaServerName, File genesisBlock) throws
            IOException, CertificateException, NoSuchAlgorithmException {

        Boolean tlsEnable = ordererUser.getTlsEnable();
        String tlsCertPem = ordererUser.getTlsCert();
        String tlsKeyPem = ordererUser.getTlsKey();
        Certificates certificate = ordererUser.getCertificate();
        String signCertPem = certificate.getCertPem();
        String signKeyPem = certificate.getKeyPem();

        String mspTempDir = mspHelper.generateNodeMsp(tlsEnable, tlsCertPem, tlsKeyPem, tlsCaServerName, signCertPem, signKeyPem);
        try {
            dockerService.copyArchiveToContainer(hostName, containerId, genesisBlock.getPath(), FABRIC_CONFIG_PATH, true);
            dockerService.copyArchiveToContainer(hostName, containerId, mspTempDir, FABRIC_CONFIG_PATH, true);
        } finally {
            //删除临时文件
            FileUtils.delete(mspTempDir);
        }
    }

    public void deleteOrderer(String ordererName) {
        OrdererNode orderer = ordererService.getOrderer(ordererName);
        if (orderer == null) throw new ServiceException("不存在名称为" + ordererName + "的orderer节点");
        String hostName = orderer.getHostName();
        String containerId = orderer.getContainerId();
        try {
            dockerService.deleteContainer(hostName, containerId);
        } finally {
            if (!dockerService.checkContainerExistent(hostName, orderer.getContainerId())) {
                ordererService.deleteByPrimaryKey(ordererName);
            }
        }
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String ordererName, DockerService.ContainerOper oper) {
        OrdererNode orderer = ordererService.getOrderer(ordererName);
        if (orderer == null) throw new ServiceException("不存在名称为" + ordererName + "的orderer节点");
        String hostName = orderer.getHostName();
        String containerId = orderer.getContainerId();
        return dockerService.changeContainerStatus(hostName, containerId, oper);
    }
}
