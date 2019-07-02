package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.controller.entity.fabricca.manage.SetCaServerEntity;
import cn.shijinshi.fabricmanager.dao.ContainerService;
import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.Container;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.FabricCaManager;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateCaEntity;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateIntermediateCaEntity;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateRootCaEntity;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.ServerConfigEntity;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.YamlFileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.exception.YamlToPojoException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FabricCaManageService {
    private static final Logger LOGGER = Logger.getLogger(FabricCaManageService.class);
    private final FabricCaManager manager;
    private final FabricCaServerService caServerService;
    private final FabricCaUserService caUserService;
    private final UserService userService;
    private final ContainerService containerService;
    private final FabricCaRequestService requester;

    @Autowired
    public FabricCaManageService(FabricCaManager manager, FabricCaServerService caServerService, FabricCaUserService caUserService,
                                 ContainerService containerService, UserService userService, FabricCaRequestService requestService) {
        this.manager = manager;
        this.caServerService = caServerService;
        this.caUserService = caUserService;
        this.containerService = containerService;
        this.userService = userService;
        this.requester = requestService;
    }

    /**
     * 创建CA服务
     */
    public DockerService.ChangeContainerStatusResult createCaServer(CreateCaEntity config, String creator, boolean rootServer) {
        //校验配置
        verityConfig(config, creator, rootServer);
        //创建CA节点
        String containerId;
        try {
            containerId = manager.createCaServer(config, rootServer);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("CA节点容器创建失败", e);
        }
        //保存CA节点容器信息
        saveCaServerInfo(creator, containerId, rootServer, config);
        //发送CA配置文件到节点容器
        ServerConfigEntity serverConfig;
        try {
            serverConfig = sendConfigToContainer(containerId, config.getHostName(), config, rootServer);
        } catch (RuntimeException e) {
            rollbackCreateCa(config.getServerName());
            throw e;
        }
        //启动CA节点
        String serverName = config.getServerName();
        DockerService.ChangeContainerStatusResult result = changeContainerStatus(serverName, DockerService.ContainerOper.START);
        if (!result.isSuccess()) {
            rollbackCreateCa(config.getServerName());
            String log = result.getLog();
            if (StringUtils.isEmpty(log)) {
                throw new ServiceException("CA节点启动失败", result.getErrMsg());
            } else {
                System.out.print(result.getLog());
                throw new ServiceException("CA节点启动失败", result.getLog());
            }
        }
        //登记注册的用户
        ServerConfigEntity.Registry registry = serverConfig.getRegistry();
        if (registry != null) {
            List<ServerConfigEntity.Registry.Identity> identities = registry.getIdentities();
            if (identities != null && !identities.isEmpty()) {
                for (ServerConfigEntity.Registry.Identity identity : identities) {
                    String userName = identity.getName();
                    requester.enrollIdentity(userName, serverName);
                }
            }
        }
        //配置组织的TLS服务
        if (rootServer) {
            Organization organization = Context.getOrganization();
            if (organization.getTlsEnable()) {
                Context.setTlsServer(serverName);
            }
        }
        return result;
    }

    private void rollbackCreateCa(String serverName) {
        try {
            deleteCaServer(serverName, true);
        } catch (RuntimeException e) {
            LOGGER.warn(e);
        }
    }


    private void verityConfig(CreateCaEntity config, String requester, boolean rootServer) {
        //目前只允许在系统内配置一个CA
        List<FabricCaServer> fabricCaServers = caServerService.selectAllServer();
        if (fabricCaServers != null && !fabricCaServers.isEmpty()) {
            throw new ServiceException("已存在一个CA" + fabricCaServers.get(0).getServerName() + "，不允许在系统中配置多个CA");
        }
        // fabric ca
        if (rootServer) {
            CreateRootCaEntity rootCaConfig = (CreateRootCaEntity) config;
            if (rootCaConfig.isUploadCert()) {
                if (rootCaConfig.getCertFile() == null || rootCaConfig.getKeyFile() == null) {
                    throw new ServiceException("请求中要求使用用户提供的根CA证书和私钥，但证书或私钥为空");
                }
            }
        } else {
            CreateIntermediateCaEntity intermediateCaConfig = (CreateIntermediateCaEntity) config;
            String parentServerName = intermediateCaConfig.getParentServerName();
            if (parentServerName == null || parentServerName.isEmpty()) {
                throw new ServiceException("CA服务类型为中间CA，但没有指定父CA名称");
            }


            try {
                caServerService.getServer(parentServerName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("不存在CA服务" + parentServerName);
            }

            String parentUserId = intermediateCaConfig.getParentUserId();
            FabricCaUser parentUser;
            try {
                parentUser = caUserService.getCaUser(parentUserId, parentServerName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("用户" + parentServerName + ":" + parentUserId + "不存在");
            }

            if (!userService.isSelfOrChild(requester, parentUser.getOwner())) {
                throw new ServiceException("用户" + requester + "无权使用CA用户" + parentServerName + ":" + parentUserId + "。");
            }
        }

        try {
            if (caServerService.getServer(config.getServerName()) != null) {
                throw new ServiceException("CA服务" + config.getServerName() + "已存在");
            }
        } catch (NotFoundBySqlException e) {
            LOGGER.debug(e);
        }


        if (StringUtils.isEmpty(config.getServerConfig().getCn())) {
            throw new ServiceException("CA所有者(CN)不可以为空");
        }
        //container
        if (StringUtils.isEmpty(config.getContainerName())) {
            throw new ServiceException("容器名称不可以为空");
        }

        int serverPort = config.getServerConfig().getServerPort();
        if (!config.getExposedPorts().containsKey(serverPort)) {
            throw new ServiceException("服务端口" + serverPort + "没有被映射到容器外，这样配置的CA无法被使用。");
        }
    }

    private void saveCaServerInfo(String creator, String containerId, boolean rootServer, CreateCaEntity config) {
        FabricCaServer caServer = new FabricCaServer();
        caServer.setServerName(config.getServerName());
        caServer.setCreator(creator);
        caServer.setHostName(config.getHostName());
        caServer.setContainerId(containerId);
        Integer serverPort = config.getServerConfig().getServerPort();
        caServer.setPort(serverPort);
        caServer.setExposedPort(config.getExposedPorts().get(serverPort));
        caServer.setHome(config.getWorkingDir());
        caServer.setTlsEnable(false);
        caServer.setTlsCa("");
        caServer.setTlsServerCert("");
        caServer.setTlsServerKey("");
        if (rootServer) {
            caServer.setParentServer("");
            caServer.setType("root");
        } else {
            CreateIntermediateCaEntity intermediateCaConfig = (CreateIntermediateCaEntity) config;
            String parentServerName = intermediateCaConfig.getParentServerName();
            FabricCaServer parentServer;
            try {
                parentServer = caServerService.getServer(parentServerName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("不存在名为" + parentServerName + "的CA服务");
            }
            if (StringUtils.isEmpty(parentServer.getParentServer())) {
                caServer.setParentServer(parentServerName);
            } else {
                caServer.setParentServer(parentServer.getParentServer() + "." + parentServerName);
            }
            caServer.setType("intermediate");
        }
        caServerService.insertServer(caServer);
    }

    private ServerConfigEntity sendConfigToContainer(String containerId, String hostName, CreateCaEntity config, boolean rootServer) {
        //读取本地CA服务配置模板（默认配置）
        String configFileTemplate = ExternalResources.getFabricCaServer("fabric-ca-server-config.yaml");
        YamlFileUtils fileUtils = new YamlFileUtils();
        ServerConfigEntity defaultConfig;
        try {
            defaultConfig = fileUtils.readYamlFile(configFileTemplate, ServerConfigEntity.class);
        } catch (YamlToPojoException e) {
            LOGGER.error(e);
            throw new ServiceException("CA配置模板文件读取失败", e);
        }
        //根据用户传入参数更新配置、发送跟新后的配置到容器中的指定位置
        String serverName = config.getServerName();
        SetCaServerEntity updateConfig = config.getServerConfig();
        if (rootServer) {
            CreateRootCaEntity rootCaConfig = (CreateRootCaEntity) config;
            if (rootCaConfig.isUploadCert()) {
                MultipartFile certFile = rootCaConfig.getCertFile();
                MultipartFile keyFile = rootCaConfig.getKeyFile();
                return manager.setCaServer(serverName, defaultConfig, updateConfig, hostName, containerId, config.getWorkingDir(), true, certFile, keyFile);
            } else {
                return manager.setCaServer(serverName, defaultConfig, updateConfig, hostName, containerId, config.getWorkingDir(), false, null, null);
            }
        } else {
            return manager.setCaServer(serverName, defaultConfig, updateConfig, hostName, containerId, config.getWorkingDir(), false, null, null);
        }
    }

    /**
     * 删除CA服务
     */
    public String deleteCaServer(String serverName, boolean force) {
        List<String> childServerNames = caServerService.selectCaChildServerName(serverName);
        if (childServerNames != null && !childServerNames.isEmpty()) {   //存在下级CA
            if (force) {
                for (String childServerName : childServerNames) {
                    manager.deleteCaServer(childServerName);
                }
            } else {
                return "删除CA服务" + serverName + "同时会删除其子服务：" + StringUtils.join(childServerNames.toArray(), ",") +
                        "是否继续删除操作？";
            }
        }
        manager.deleteCaServer(serverName);
        return null;
    }

    /**
     * 改变CA服务的容器运行状态
     */
    public DockerService.ChangeContainerStatusResult changeContainerStatus(String serverName, DockerService.ContainerOper oper) {
        return manager.changeContainerStatus(serverName, oper);
    }

    /**
     * 获取本地fabric ca默认配置文件
     */
    public SetCaServerEntity getDefaultConfig() {
        //读取本地配置模板（默认配置）
        String configFileTemplate = ExternalResources.getFabricCaServer("fabric-ca-server-config.yaml");
        YamlFileUtils fileUtils = new YamlFileUtils();
        ServerConfigEntity defaultConfig;
        try {
            defaultConfig = fileUtils.readYamlFile(configFileTemplate, ServerConfigEntity.class);
        } catch (YamlToPojoException e) {
            LOGGER.error(e);
            throw new ServiceException("CA配置模板文件读取失败", e);
        }
        //构建返回给前端的对象
        SetCaServerEntity config = new SetCaServerEntity();
        config.setAffiliations(defaultConfig.getAffiliations());
        Map signing = defaultConfig.getSigning();
        if (signing != null && signing.containsKey("default")) {
            Map aDefault = (Map) signing.get("default");
            if (aDefault.containsKey("expiry")) {
                config.setCertExpiry((String) (aDefault.get("expiry")));
            }
        }

        ServerConfigEntity.Csr csr = defaultConfig.getCsr();
        if (csr != null) {
            config.setCn(csr.getCn());
            List<Map<String, String>> names = csr.getNames();
            if (names != null && !names.isEmpty()) {
                config.setCsrName(names.get(0));
            }
        }

        ServerConfigEntity.Crl crl = defaultConfig.getCrl();
        if (crl != null) {
            config.setCrlExpiry(crl.getExpiry());
        }

        ServerConfigEntity.Registry registry = defaultConfig.getRegistry();
        if (registry != null) {
            List<ServerConfigEntity.Registry.Identity> identities = registry.getIdentities();
            if (identities != null && !identities.isEmpty()) {
                config.setUser(identities.get(0).getName());
            }
        }

        config.setDebug(defaultConfig.getDebug());
        config.setServerPort(defaultConfig.getPort());

        return config;
    }

    /**
     * 从容器中获取CA服务配置文件
     */
    public Map getServerConfig(String serverName) {
        Container serverContainer = containerService.selectCAContainer(serverName);
        String hostName = serverContainer.getHostName();
        String containerId = serverContainer.getContainerId();
        String home;
        try {
            home = caServerService.getServer(serverName).getHome();
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + serverName + "的CA服务");
        }
        ServerConfigEntity serverConfig = manager.getServerConfig(serverName, home, hostName, containerId);

        Map<String, Object> data = new HashMap<>();
        data.put("debug", serverConfig.getDebug());
        ServerConfigEntity.Crl crl = serverConfig.getCrl();
        if (crl != null) {
            data.put("crlExpiry", crl.getExpiry());
        } else {
            data.put("crlExpiry", "");
        }

        Map signing = serverConfig.getSigning();
        if (signing != null && signing.containsKey("default")) {
            Map aDefault = (Map) signing.get("default");
            if (aDefault != null && aDefault.containsKey("expiry")) {
                data.put("certExpiry", aDefault.get("expiry"));
            } else {
                data.put("certExpiry", "");
            }
        } else {
            data.put("certExpiry", "");
        }

        return data;
    }

    /**
     * 修改指定CA的配置
     */
    public DockerService.ChangeContainerStatusResult setServerConfig(String serverName, SetCaServerEntity updateConfig) {
        //获取CA服务原有配置
        FabricCaServer caServer;
        try {
            caServer = caServerService.getServer(serverName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + serverName + "的CA服务");
        }
        String hostName = caServer.getHostName();
        String containerId = caServer.getContainerId();
        String home = caServer.getHome();
        ServerConfigEntity serverConfig = manager.getServerConfig(serverName, home, hostName, containerId);
        //CA服务创建后只允许更新以下配置
        SetCaServerEntity update = new SetCaServerEntity();
        if (null != updateConfig.getDebug()) {
            update.setDebug(updateConfig.getDebug());
        }
        if (null != updateConfig.getCrlExpiry()) {
            update.setCrlExpiry(updateConfig.getCrlExpiry());
        }
        if (null != updateConfig.getCertExpiry()) {
            update.setCertExpiry(updateConfig.getCertExpiry());
        }
        //更新服务配置文件
        try {
            manager.setCaServer(serverName, serverConfig, update, hostName, containerId, home, false, null, null);
        } catch (InternalServerErrorException e){
            LOGGER.warn(e);
            throw new ServiceException("CA配置更新失败，无法更新CA容器内的配置文件", e);
        } catch (RuntimeException e) {
            LOGGER.warn(e);
            throw new ServiceException("CA配置更新失败", e);
        }
        //重启docker服务，使得新的配置生效
        return changeContainerStatus(serverName, DockerService.ContainerOper.RESTART);
    }

    /**
     * 获取指定CA的根证书
     */
    public File getCaCert(String serverName) {
        Container serverContainer = containerService.selectCAContainer(serverName);
        if (serverContainer == null) {
            throw new ServiceException("无法获取到CA服务" + serverName + "对应的容器信息");
        }
        String hostName = serverContainer.getHostName();
        String containerId = serverContainer.getContainerId();
        String home;
        try {
            home = caServerService.getServer(serverName).getHome();
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + serverName + "的CA服务");
        }
        return manager.getCaCert(serverName, home, hostName, containerId);
    }
}
