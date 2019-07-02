package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.controller.entity.couchdb.CreateCouchdbEntity;
import cn.shijinshi.fabricmanager.dao.CouchdbNodeService;
import cn.shijinshi.fabricmanager.dao.entity.CouchdbNode;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.node.couchdb.CouchdbManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouchdbService {
    private static final Logger LOGGER = Logger.getLogger(CouchdbService.class);
    private final CouchdbManager manager;
    private final CouchdbNodeService couchdbService;


    @Autowired
    public CouchdbService(CouchdbManager manager, CouchdbNodeService couchdbNodeService) {
        this.manager = manager;
        this.couchdbService = couchdbNodeService;
    }


    public DockerService.ChangeContainerStatusResult createCouchdb(String creator, String peerName, CreateCouchdbEntity config) {
        //检查配置是否符合要求
        verityConfig(config);
        String containerId;
        try {
            containerId = manager.createCouchdb(config);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("Couchdb节点创建失败", e);
        }
        //保存couchdb节点信息到数据库
        saveCouchdbInfo(creator, peerName, containerId, config);
        //启动节点容器
        String couchdbName = config.getCouchdbName();
        DockerService.ChangeContainerStatusResult result = changeContainerStatus(couchdbName, DockerService.ContainerOper.START);
        if (!result.isSuccess()) {
            rollbackCreateCouchdb(couchdbName);
            String log = result.getLog();
            if (StringUtils.isEmpty(log)) {
                throw new ServiceException("Couchdb节点启动失败", result.getErrMsg());
            } else {
                System.out.print(result.getLog());
                throw new ServiceException("Couchdb节点启动失败", result.getLog());
            }
        }
        return result;
    }

    private void rollbackCreateCouchdb(String couchdbName) {
        try {
            deleteCouchdb(couchdbName);
        }catch (RuntimeException e) {
            LOGGER.warn(e);
        }
    }

    private void verityConfig(CreateCouchdbEntity config) {
        //couchdb
        CouchdbNode couchdbNode = couchdbService.selectByPrimaryKey(config.getCouchdbName());
        if (couchdbNode != null) {
            throw new ServiceException("已存在couchdb节点" + config.getCouchdbName() + "请处理冲突,更换couchdb节点名称可解决冲突。");
        }

        //container
        if (StringUtils.isEmpty(config.getCouchdbName())) {
            throw new ServiceException("Couchdb节点容器不可以为空");
        }

        Integer serverPort = config.getServerPort();
        if (!config.getExposedPorts().containsKey(serverPort)) {
            throw new ServiceException("Couchdb服务端口" + serverPort + "没有被映射到容器外。");
        }
    }

    /**
     * //保存couchdb节点信息到数据库
     */
    private void saveCouchdbInfo(String creator, String peerName, String containerId, CreateCouchdbEntity config) {
        CouchdbNode couchdb = new CouchdbNode();
        couchdb.setCouchdbName(config.getCouchdbName());
        couchdb.setCreator(creator);
        couchdb.setHostName(config.getHostName());
        couchdb.setContainerId(containerId);
        couchdb.setPeerName(peerName);
        Integer serverPort = config.getServerPort();
        couchdb.setPort(serverPort);
        couchdb.setExposedPort(config.getExposedPorts().get(serverPort));
        couchdbService.insertSelective(couchdb);
    }

    public void deleteCouchdb(String couchdbName) {
        manager.deleteCouchdb(couchdbName);
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String couchdbName, DockerService.ContainerOper oper) {
        return manager.changeContainerStatus(couchdbName, oper);
    }

    public List<CouchdbNode> getCouchdbs() {
        return couchdbService.selectAllCouchdb();
    }
}
