package cn.shijinshi.fabricmanager.service.fabric.node.couchdb;

import cn.shijinshi.fabricmanager.controller.entity.couchdb.CreateCouchdbEntity;
import cn.shijinshi.fabricmanager.dao.CouchdbNodeService;
import cn.shijinshi.fabricmanager.dao.entity.CouchdbNode;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.DockerService;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CouchdbManager {
    private static Logger log = Logger.getLogger(CouchdbManager.class);

    private final CouchdbNodeService couchdbService;
    private final DockerService dockerService;

    @Autowired
    public CouchdbManager(CouchdbNodeService couchdbService, DockerService dockerService) {
        this.couchdbService = couchdbService;
        this.dockerService = dockerService;
    }






    public String createCouchdb(CreateCouchdbEntity config) {
        //设置couchdb默认配置、根据用户传入参数生成配置
        setDefaultConfig(config);
        //创建容器
        CreateContainerResponse response = dockerService.createContainer(config);
        return response.getId();
    }


    private void setDefaultConfig(CreateCouchdbEntity config) {
        //设置默认配置
        if (StringUtils.isEmpty(config.getImage())) {
            config.setImage("hyperledger/fabric-couchdb");
        }
    }


    public void deleteCouchdb(String couchdbName) {
        CouchdbNode couchdb = couchdbService.selectByPrimaryKey(couchdbName);
        if (couchdb == null) throw new ServiceException("Couchdb节点删除失败，不存在名称为" + couchdbName + "的couchdb节点");
        String hostName = couchdb.getHostName();
        String containerId = couchdb.getContainerId();
        try {
            dockerService.deleteContainer(hostName, containerId);
        } finally {
            if (!dockerService.checkContainerExistent(hostName, containerId)) {
                couchdbService.deleteByPrimaryKey(couchdbName);
            }
        }
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String couchdbName, DockerService.ContainerOper oper) {
        CouchdbNode couchdb = couchdbService.selectByPrimaryKey(couchdbName);
        if (couchdb == null) throw new ServiceException("不存在名称为" + couchdbName + "的couchdb节点");
        String hostName = couchdb.getHostName();
        String containerId = couchdb.getContainerId();
        return dockerService.changeContainerStatus(hostName, containerId, oper);
    }
}
