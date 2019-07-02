package cn.shijinshi.fabricmanager.service.fabric.node.peer;

import cn.shijinshi.fabricmanager.controller.entity.peer.CreatePeerEntity;
import cn.shijinshi.fabricmanager.dao.PeerNodeService;
import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.PeerNode;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.CouchdbService;
import cn.shijinshi.fabricmanager.service.DockerService;
import cn.shijinshi.fabricmanager.service.fabric.certificate.MspHelper;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PeerManager {
    private static final Logger LOGGER = Logger.getLogger(PeerManager.class);
    private static final String FABRIC_CONFIG_PATH = "/home/";
    private static final String MSP_PATH = FABRIC_CONFIG_PATH + "msp";
    private static final String TLS_PATH = FABRIC_CONFIG_PATH + "tls";

    @Autowired
    private PeerNodeService peerService;

    @Autowired
    private MspHelper mspHelper;
    @Autowired
    private DockerService dockerService;
    @Autowired
    private CouchdbService couchdbService;


    /**
     * 创建Peer节点容器
     *
     * @param config   节点配置
     * @param org      组织信息
     * @param peerUser 节点身份信息
     * @return 容器ID
     */
    public String createPeer(CreatePeerEntity config, Organization org, UserAndCerts peerUser) {
        //设置Peer默认配置、根据用户传入参数生成配置
        setDefaultConfig(config, org.getOrgMspId(), peerUser.getTlsEnable());
        //创建peer节点容器
        CreateContainerResponse response = dockerService.createContainer(config);
        return response.getId();

    }


    private void setDefaultConfig(CreatePeerEntity config, String mspId, boolean tlsEnable) {
        //设置默认配置
        if (StringUtils.isEmpty(config.getWorkingDir())) {
            config.setWorkingDir(FABRIC_CONFIG_PATH);
        }
        if (StringUtils.isEmpty(config.getCmd())) {
            config.setCmd("peer node start");
        }
        if (config.getServerPort() == 0) {
            config.setServerPort(7051);
        }

        //设置默认的环境变量
        Map<String, String> envs = config.getEnv();
        if (envs == null) {
            envs = new HashMap<>();
            config.setEnv(envs);
        }
        envs.put("GODEBUG", "netdns=go");
        envs.put("CORE_PEER_ID", config.getContainerName());
        envs.put("CORE_PEER_ADDRESS", config.getContainerName() + ":" + "7051");
        envs.put("CORE_PEER_CHAINCODEADDRESS", config.getContainerName() + ":" + "7052");
        envs.put("CORE_PEER_CHAINCODELISTENADDRESS", "0.0.0.0:7052");
        envs.put("CORE_PEER_GOSSIP_EXTERNALENDPOINT", config.getContainerName() + ":" + "7051");
//        envs.put("CORE_PEER_GOSSIP_BOOTSTRAP", "peer0.org1.example.com:7051");
        envs.put("CORE_PEER_LOCALMSPID", mspId);



        envs.put("CORE_VM_ENDPOINT", "unix:///host/var/run/docker.sock");
        /**
         * the following setting starts chaincode containers on the same
         * bridge networkManage as the peers
         * https://docs.docker.com/compose/networking/
         */
        envs.put("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE", config.getNetworkMode());      //peer所在的docker网络
        envs.put("CORE_LOGGING_LEVEL", "DEBUG");
        envs.put("CORE_PEER_ENDORSER_ENABLED", "true");                 //endoeser peer
        /**
         * CORE_PEER_GOSSIP_USELEADERELECTION 和 CORE_PEER_GOSSIP_ORGLEADER
         * 如果都配置为false，那么peer不会尝试变成一个leader，这种情况下静态配置的leader由使用者自行保证其可用性
         * 如果都配置为true，会引起异常
         */
        envs.put("CORE_PEER_GOSSIP_USELEADERELECTION", "true");     //使用节点选举
        envs.put("CORE_PEER_GOSSIP_ORGLEADER", "false");            //本节点作为组织的leader，不可与CORE_PEER_GOSSIP_USELEADERELECTION同时为true
//        envs.put("CORE_PEER_GOSSIP_SKIPHANDSHAKE", "true");         //是否跳过gossip握手
        envs.put("CORE_PEER_PROFILE_ENABLED", "true");
        envs.put("CORE_PEER_CHANNELSERVICE_ENABLED", "true");       //通道服务是否可用

        if (config.getCouchdbEnable()) {
            envs.put("CORE_LEDGER_STATE_STATEDATABASE", "CouchDB");
            envs.put("CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS", config.getCouchdbContainerName() + ":" + "5984");
        }

        //设置MSP
        envs.put("CORE_PEER_MSPCONFIGPATH", MSP_PATH);
        //设置TLS
        if (tlsEnable) {
            envs.put("CORE_PEER_TLS_ENABLED", "true");
            envs.put("CORE_PEER_TLS_CERT_FILE", TLS_PATH + "/server.crt");
            envs.put("CORE_PEER_TLS_KEY_FILE", TLS_PATH + "/server.key");
            envs.put("CORE_PEER_TLS_ROOTCERT_FILE", TLS_PATH + "/ca.crt");
        } else {
            envs.put("CORE_PEER_TLS_ENABLED", "false");
        }
    }


    /**
     * 发送本地的TLS证书和MSP文件到容器中的指定目录下
     */
    public void sendMspToContainer(String containerId, String hostName, UserAndCerts peerUser, String tlsCaServerName)
            throws NoSuchAlgorithmException, CertificateException, IOException {

        Boolean tlsEnable = peerUser.getTlsEnable();
        String tlsCertPem = peerUser.getTlsCert();
        String tlsKeyPem = peerUser.getTlsKey();
        Certificates certificate = peerUser.getCertificate();
        String signCertPem = certificate.getCertPem();
        String signKeyPem = certificate.getKeyPem();

        String mspTempDir = mspHelper.generateNodeMsp(tlsEnable, tlsCertPem, tlsKeyPem, tlsCaServerName, signCertPem, signKeyPem);

        try {
            //拷贝TLS和MSP文件到容器中，容器中存放文件的路径FABRIC_CONFIG_PATH与容器的环境变量中相关路径保持一致
            dockerService.copyArchiveToContainer(hostName, containerId, mspTempDir, FABRIC_CONFIG_PATH, true);
        } finally {
            //删除临时文件
            FileUtils.delete(mspTempDir);
        }
    }


    public void deletePeer(String peerName) {
        PeerNode peer = peerService.selectByPrimaryKey(peerName);
        if (peer == null) throw new ServiceException("Peer节点删除失败，不存在名称为" + peerName + "的peer节点");
        String hostName = peer.getHostName();
        //删除couchdb
        if (peer.getCouchdbEnable()) {
            String couchdbName = peer.getCouchdbName();
            couchdbService.deleteCouchdb(couchdbName);
        }
        //删除Peer
        try {
            dockerService.deleteContainer(hostName, peer.getContainerId());
        } finally {
            if (!dockerService.checkContainerExistent(hostName, peer.getContainerId())) {
                peerService.deleteByPrimaryKey(peerName);
            }
        }
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String peerName, DockerService.ContainerOper oper) {
        PeerNode peer = peerService.selectByPrimaryKey(peerName);
        if (peer == null) throw new ServiceException("不存在名称为" + peerName + "的peer节点");
        String hostName = peer.getHostName();
        String containerId = peer.getContainerId();
        return dockerService.changeContainerStatus(hostName, containerId, oper);
    }
}
