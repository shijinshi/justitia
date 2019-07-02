package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.controller.entity.orderer.CreateOrdererEntity;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.OrdererNodeService;
import cn.shijinshi.fabricmanager.dao.entity.OrdererAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.OrdererNode;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.node.orderer.OrdererManager;
import cn.shijinshi.fabricmanager.service.fabric.tools.ConfigTxGen;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdererService {
    private static final Logger LOGGER = Logger.getLogger(OrdererService.class);
    private final OrdererNodeService ordererService;
    private final FabricCaUserService caUserService;
    private final OrdererManager manager;
    private final ConfigTxGen configtxgen;
    private final HostManageService hostManageService;

    @Autowired
    public OrdererService(OrdererNodeService ordererService, FabricCaUserService caUserService, OrdererManager manager,
                          ConfigTxGen configtxgen, HostManageService hostManageService) {
        this.ordererService = ordererService;
        this.caUserService = caUserService;
        this.manager = manager;
        this.configtxgen = configtxgen;
        this.hostManageService = hostManageService;
    }

    public DockerService.ChangeContainerStatusResult createOrderer(String creator, CreateOrdererEntity config) {
        Organization org = Context.getOrganization();
        String caServerName = config.getCaServerName();
        String caOrdererUser = config.getOrdererUserId();
        if (StringUtils.isEmpty(caServerName) || StringUtils.isEmpty(caOrdererUser)) {
            throw new ServiceException("创建Orderer节点必须指定一个有效的CA中Orderer用户");
        }
        UserAndCerts ordererUser;
        try {
            ordererUser = caUserService.getUserCerts(caOrdererUser, caServerName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("没有找到指定用户(" + caServerName +":" + caOrdererUser +")的相关信息");
        }

        //检查配置是否符合要求
        verityConfig(config, org.getOrgType());
        //创建orderer节点
        String containerId;
        try {
            containerId = manager.createOrderer(config, org, ordererUser);
        }catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("Orderer节点容器创建失败", e);
        }
        //保存Orderer节点信息到数据库
        saveOrdererInfo(creator, containerId, config);
        //发送配置文件到容器中
        try {
            ArrayList<String> ordererAddresses = new ArrayList<>();
            String ip = hostManageService.getHost(config.getHostName()).getIp();
            Integer port = config.getExposedPorts().get(config.getServerPort());
            ordererAddresses.add(ip + ":" + port);
            File genesisBlock = configtxgen.createGenesisBlock(config.getSystemChainId(), config.getConsortiumName(), ordererAddresses);
            try {
                manager.sendMspToContainer(containerId, config.getHostName(), ordererUser, org.getTlsCaServer(), genesisBlock);
            } finally {
                FileUtils.delete(genesisBlock.getParent());
            }
        } catch (Exception e) {
            LOGGER.error(e);
            rollbackCreateOrderer(config.getOrdererName());
            throw new ServiceException("配置orderer节点容器失败", e);
        }
        //启动Orderer节点
        String ordererName = config.getOrdererName();
        DockerService.ChangeContainerStatusResult result = changeContainerStatus(ordererName, DockerService.ContainerOper.START);
        if (!result.isSuccess()) {
            rollbackCreateOrderer(ordererName);
            String log = result.getLog();
            if (StringUtils.isEmpty(log)) {
                throw new ServiceException("Orderer节点启动失败", result.getErrMsg());
            } else {
                throw new ServiceException("Orderer节点启动失败", result.getLog());
            }
        }
        return result;
    }

    private void rollbackCreateOrderer(String ordererName) {
        try {
            deleteOrderer(ordererName);
        } catch (RuntimeException e) {
            LOGGER.warn(e);
        }
    }
    private void verityConfig(CreateOrdererEntity config, String orgType) {
        //orderer
        if (!Organization.ORG_TYPE_ORDERER.equals(orgType)) {
            throw new ServiceException("本组织不能创建Peer节点。");
        }
        OrdererNode ordererNode = ordererService.getOrderer(config.getOrdererName());
        if (ordererNode != null) {
            throw new ServiceException("Orderer节点" + config.getOrdererName() + "已存在");
        }
        //容器配置
        if (StringUtils.isEmpty(config.getContainerName())) {
            throw new ServiceException("容器名称不可以为空");
        }
    }



    private void saveOrdererInfo(String creator, String containerId, CreateOrdererEntity config) {
        OrdererNode orderer = new OrdererNode();
        orderer.setOrdererName(config.getOrdererName());
        orderer.setServerPort(config.getServerPort());
        orderer.setCaServerName(config.getCaServerName());
        orderer.setCaOrdererUser(config.getOrdererUserId());
        orderer.setCreator(creator);
        orderer.setHostName(config.getHostName());
        orderer.setContainerId(containerId);
        orderer.setSystemChain("testchainid");
        ordererService.insertSelective(orderer);
    }

    public void deleteOrderer(String ordererName) {
        manager.deleteOrderer(ordererName);
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String ordererName, DockerService.ContainerOper oper)  {
        return manager.changeContainerStatus(ordererName, oper);
    }

    public List<Map> getLocalOrderers() {
        List<OrdererAndContainer> ordererAndContainers = ordererService.selectAllOrderer();
        List<Map> res = new ArrayList<>();
        if (ordererAndContainers != null) {
            for (OrdererAndContainer ordererAndContainer : ordererAndContainers) {
                Map<String, Object> ordererInfo = new HashMap();
                ordererInfo.put("ordererName", ordererAndContainer.getOrdererName());
                ordererInfo.put("hostName", ordererAndContainer.getHostName());
                ordererInfo.put("containerId", ordererAndContainer.getContainerId());
                ordererInfo.put("creator", ordererAndContainer.getCreator());
                ordererInfo.put("dockerNetwork", ordererAndContainer.getNetworkMode());
                //fixme 这个取值换成orderer节点对应用户的tlsenable更加合适一些
                ordererInfo.put("tlsEnable", Context.getOrganization().getTlsEnable());

                res.add(ordererInfo);
            }
        }
        return res;
    }
}
