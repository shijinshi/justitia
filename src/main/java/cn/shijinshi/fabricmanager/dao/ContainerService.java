package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Container;
import cn.shijinshi.fabricmanager.dao.mapper.ContainerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContainerService {
    @Autowired
    private ContainerMapper mapper;

    public int insertContainer(Container container) {
        return mapper.insert(container);
    }

    public int deleteContainer(String hostName, String containerId) {
        return mapper.deleteByPrimaryKey(hostName, containerId);
    }

    public Container selectCAContainer(String serverName) {
        return mapper.selectCaContainer(serverName);
    }

}