package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.controller.entity.couchdb.CreateCouchdbEntity;
import cn.shijinshi.fabricmanager.controller.entity.peer.CreatePeerEntity;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.PeerNodeService;
import cn.shijinshi.fabricmanager.dao.PeerRefChannelService;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.PeerAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.PeerNode;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.node.peer.PeerManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PeerService {
    private static final Logger LOGGER = Logger.getLogger(PeerService.class);
    private final PeerNodeService peerService;
    private final FabricCaUserService caUserService;
    private final PeerManager manager;
    private final CouchdbService couchdbService;
    private final PeerRefChannelService peerRefChannelService;

    @Autowired
    public PeerService(PeerNodeService peerService, FabricCaUserService caUserService, PeerManager manager,
                       CouchdbService couchdbService, PeerRefChannelService peerRefChannelService) {
        this.peerService = peerService;
        this.caUserService = caUserService;
        this.manager = manager;
        this.couchdbService = couchdbService;
        this.peerRefChannelService = peerRefChannelService;
    }


    public DockerService.ChangeContainerStatusResult createPeer(String creator, CreatePeerEntity config) {
        Organization org = Context.getOrganization();
        String caServerName = config.getCaServerName();
        String caPeerUser = config.getPeerUserId();
        if (StringUtils.isEmpty(caServerName) || StringUtils.isEmpty(caPeerUser)) {
            throw new ServiceException("创建Peer节点必须指定一个有效的CA中Peer用户");
        }
        UserAndCerts peerUser;
        try {
            peerUser = caUserService.getUserCerts(caPeerUser, caServerName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("没有找到指定用户(" + caServerName + ":" + caPeerUser + ")的相关信息");
        }

        //检查配置是否符合要求
        verityConfig(config, org.getOrgType());
        //创建couchdb节点
        String couchdbName = null;
        if (config.getCouchdbEnable()) {
            couchdbName = createCouchdb(creator, config);
        }
        //创建peer节点
        String containerId;
        try {
            containerId = manager.createPeer(config, org, peerUser);
        } catch (Exception e) {
            LOGGER.error(e);
            if (StringUtils.isNotEmpty(couchdbName)) {
                try {
                    couchdbService.deleteCouchdb(couchdbName);
                } catch (Exception e1) {
                    LOGGER.warn("couchdb node delete failed.", e1);
                }
            }
            throw new ServiceException("Peer节点容器创建失败", e);
        }
        //保存peer节点信息到数据库
        savePeerInfo(creator, containerId, couchdbName, config);
        //发送配置文件到容器中
        try {
            manager.sendMspToContainer(containerId, config.getHostName(), peerUser, org.getTlsCaServer());
        } catch (Exception e) {
            LOGGER.error(e);
            rollbackCreatePeer(config.getPeerName());
            throw new ServiceException("配置Peer节点容器失败", e);
        }
        //初次启动peer节点
        String peerName = config.getPeerName();
        DockerService.ChangeContainerStatusResult result = changeContainerStatus(peerName, DockerService.ContainerOper.START);
        if (!result.isSuccess()) {
            rollbackCreatePeer(peerName);
            String log = result.getLog();
            if (StringUtils.isEmpty(log)) {
                throw new ServiceException("Peer节点启动失败", result.getErrMsg());
            } else {
                throw new ServiceException("Peer节点启动失败", result.getLog());
            }
        }
        return result;
    }

    private void rollbackCreatePeer(String peerName) {
        try {
            deletePeer(peerName);
        } catch (RuntimeException e) {
            LOGGER.warn("peer node delete failed.", e);
        }
    }


    /**
     * 检查创建节点的配置是否符合我要求
     */
    private void verityConfig(CreatePeerEntity config, String orgType) {
        //peer
        if (!Organization.ORG_TYPE_PEER.equals(orgType)) {
            throw new ServiceException("本组织不能创建Orderer节点。");
        }
        PeerNode peerNode = peerService.selectByPrimaryKey(config.getPeerName());
        if (peerNode != null) {
            throw new ServiceException("Peer节点" + config.getPeerName() + "已存在");
        }
        //容器配置
        if (StringUtils.isEmpty(config.getContainerName())) {
            throw new ServiceException("Peer节点容器名称不可以为空");
        }
        //挂载卷
        Map<String, String> volumes = config.getVolumes();
        if (!volumes.containsValue("/host/var/run/") && !volumes.containsValue("/host/var/run")) {
            throw new ServiceException("容器内路径/host/var/run/需要挂载到docker的运行目录（默认为/var/run/）");
        }
    }

    /**
     * 创建couchdb节点
     */
    private String createCouchdb(String creator, CreatePeerEntity config) {
        CreateCouchdbEntity couchdbConfig = new CreateCouchdbEntity();
        int couchdbServerPort = 5984;

        //couchdb
        String couchdbName = config.getPeerName() + "-couchdb";
        couchdbConfig.setCouchdbName(couchdbName);
        couchdbConfig.setHostName(config.getHostName());
        couchdbConfig.setServerPort(couchdbServerPort);
        //container
        String couchdbContainerName = config.getCouchdbContainerName();
        if (StringUtils.isEmpty(couchdbContainerName)) {
            couchdbContainerName = couchdbName;
        }
        couchdbConfig.setContainerName(couchdbContainerName);
        couchdbConfig.setImage(config.getCouchdbImage());
        couchdbConfig.setTag(config.getCouchdbTag());
        Map<Integer, Integer> exposedPorts = new HashMap<>();
        exposedPorts.put(couchdbServerPort, config.getCouchdbExposedPort());
        couchdbConfig.setExposedPorts(exposedPorts);
        couchdbConfig.setNetworkMode(config.getNetworkMode());

        DockerService.ChangeContainerStatusResult result = couchdbService.createCouchdb(creator, config.getPeerName(), couchdbConfig);
        if (result.isSuccess()) {
            return couchdbName;
        } else {
            throw new ServiceException("Couchdb节点启动失败:" + result.getErrMsg());
        }
    }

    /**
     * 保存peer节点信息到数据库
     */
    private void savePeerInfo(String creator, String containerId, String couchdbName, CreatePeerEntity config) {
        PeerNode peerNode = new PeerNode();
        peerNode.setPeerName(config.getPeerName());
        peerNode.setServerPort(config.getServerPort());
        peerNode.setCaServerName(config.getCaServerName());
        peerNode.setCaPeerUser(config.getPeerUserId());
        peerNode.setCreator(creator);
        peerNode.setHostName(config.getHostName());
        peerNode.setContainerId(containerId);
        if (StringUtils.isNotEmpty(couchdbName)) {
            peerNode.setCouchdbEnable(true);
            peerNode.setCouchdbName(couchdbName);
        } else {
            peerNode.setCouchdbEnable(false);
        }
        peerService.insertSelective(peerNode);
    }

    /**
     * delete peer and couchdb node
     * @param peerName
     */
    public void deletePeer(String peerName) {
        manager.deletePeer(peerName);
        peerRefChannelService.deleteByPeer(peerName);
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String peerName, DockerService.ContainerOper oper) {
        return manager.changeContainerStatus(peerName, oper);
    }

    public List<Map> getLocalPeers() {
        List<Map> res = new ArrayList<>();
        List<PeerAndContainer> peerAndContainers = peerService.selectAllPeer();
        if (peerAndContainers != null && !peerAndContainers.isEmpty()) {
            for (PeerAndContainer peerAndContainer : peerAndContainers) {
                Map<String, Object> peerInfo = new HashMap<>();
                peerInfo.put("peerName", peerAndContainer.getPeerName());
                peerInfo.put("hostName", peerAndContainer.getHostName());
                peerInfo.put("containerName", peerAndContainer.getContainerName());
                peerInfo.put("containerId", peerAndContainer.getContainerId());
                peerInfo.put("couchdbName", peerAndContainer.getCouchdbName());
                //fixme 这个取值换成peer节点对应用户的tlsenable更加合适一些
                peerInfo.put("tlsEnable", Context.getOrganization().getTlsEnable());

                res.add(peerInfo);
            }
        }
        return res;
    }
}
