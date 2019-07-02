package cn.shijinshi.fabricmanager.service.fabric.node.fabricca;

import cn.shijinshi.fabricmanager.controller.entity.fabricca.manage.SetCaServerEntity;
import cn.shijinshi.fabricmanager.dao.ContainerService;
import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.HostService;
import cn.shijinshi.fabricmanager.dao.entity.Container;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.entity.Host;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.DockerService;
import cn.shijinshi.fabricmanager.service.fabric.docker.exception.ContainerConfigException;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateCaEntity;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateIntermediateCaEntity;
import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.ServerConfigEntity;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.MultipartFileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.YamlFileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.exception.YamlToPojoException;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FabricCaManager {
    private static final String CONFIG_FILE_NAME = "fabric-ca-server-config.yaml";
    private static final Logger LOGGER = Logger.getLogger(FabricCaManager.class);

    @Autowired
    private HostService hostService;
    @Autowired
    private FabricCaServerService caServerService;
    @Autowired
    private FabricCaUserService caUserService;
    @Autowired
    private ContainerService containerService;

    @Autowired
    private DockerService dockerService;

    //------------------------------------ fabric ca container manage ------------------------------------------------
    public String createCaServer(CreateCaEntity config, boolean rootServer) {
        //设置默认配置，根据用户传入参数生成配置
        setDefaultConfig(config, rootServer);
        //创建容器
        CreateContainerResponse response = dockerService.createContainer(config);
        return response.getId();
    }


    private void setDefaultConfig(CreateCaEntity config, boolean rootServer) {
        //fabric ca
        SetCaServerEntity serverConfig = config.getServerConfig();
        Integer serverPort = serverConfig.getServerPort();
        if (serverPort == null || serverPort == 0) {
            serverConfig.setServerPort(7054);
        }
        //container
        if (StringUtils.isEmpty(config.getImage())) {
            config.setImage("hyperledger/fabric-ca");
        }
        if (StringUtils.isEmpty(config.getWorkingDir())) {
            config.setWorkingDir("/etc/hyperledger/fabric-ca-server/");
        }
        if (StringUtils.isEmpty(config.getCmd())) {
            if (rootServer) {
                config.setCmd(createCmd(serverConfig.getServerPort(), true, null));
            } else {
                CreateIntermediateCaEntity intermediateCaConfig = (CreateIntermediateCaEntity) config;
                String parentServerUrl = getParentServerUrl(intermediateCaConfig.getParentUserId(), intermediateCaConfig.getParentServerName());
                config.setCmd(createCmd(serverConfig.getServerPort(), false, parentServerUrl));
            }
        }
        //环境变量
        Map<String, String> envs = config.getEnv();
        String fabric_ca_home = envs.get("FABRIC_CA_HOME");
        if (fabric_ca_home == null || fabric_ca_home.isEmpty()) {
            envs.put("FABRIC_CA_HOME", "/etc/hyperledger/fabric-ca-server");
        }
    }

    private String getParentServerUrl(String parentUserId, String parentServerName) {
        FabricCaServer parentServer;
        try {
            parentServer = caServerService.getServer(parentServerName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + parentServerName +"的CA服务");
        }
        FabricCaUser parentUser;
        try {
            parentUser = caUserService.getCaUser(parentUserId, parentServerName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为("+parentServerName+":"+parentUserId+")的用户");
        }
        StringBuilder sb = new StringBuilder();
        if (parentServer.getTlsEnable()) {
            sb.append("https://");
            //todo https时需要tls证书， 默认CA不使用TLS
        } else {
            sb.append("http://");
        }
        sb.append(parentUser.getUserId());
        sb.append(":");
        sb.append(parentUser.getSecret());
        sb.append("@");
        Host parentHost;
        try {
            parentHost = hostService.getHost(parentServer.getHostName());
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" + parentServer.getHostName() +"的主机");
        }
        sb.append(parentHost.getIp());
        sb.append(":");
        sb.append(parentServer.getExposedPort());
        return sb.toString();
    }

    private String createCmd(int port, boolean rootServer, String parentUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("fabric-ca-server start ");
        sb.append(" -p ").append(port);
        if (!rootServer) {
            //FIXME 将-u参数放在容器启动命令会不会导致容器重启时重新注册中间CA证书，这个情况有待验证
            if (StringUtils.isEmpty(parentUrl)) {
                throw new IllegalArgumentException("当CA类型为中间CA时，必须指定父CA的请求地址");
            }
            sb.append(" -u ").append(parentUrl);
        }
        return sb.toString();
    }


    /**
     * 读取CA服务的配置文件，并将部分数据写入数据库
     *
     * @param serverName  CA服务名称
     * @param hostName    CA服务所在主机
     * @param containerId 容器名称
     * @throws IOException
     */
    private void getCaConfig2Db(String serverName, String hostName, String containerId) {
        FabricCaServer caServer;
        try {
            caServer = caServerService.getServer(serverName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名为" +serverName + "的CA服务");
        }
        String home = caServer.getHome();
        ServerConfigEntity serverConfig = getServerConfig(serverName, home, hostName, containerId);
        //配置文件中注册的身份（用户）
        List<FabricCaUser> fabricCaUsers = parseRegistry(serverConfig.getRegistry());
        for (FabricCaUser caUser : fabricCaUsers) {
            caUser.setServerName(serverName);
            caUser.setCreator(caServer.getCreator());
            caUser.setOwner(caServer.getCreator());
            caUser.setState(FabricCaUser.STATE_REGISTERED);
            caUser.setTlsEnable(false);
            caUser.setTlsCert(null);
            caUser.setTlsCert(null);

            caUserService.insertUser(caUser);
        }
        //配置文件中设定的成员关系
        caServerService.updateAffiliations(serverName, JSONObject.valueToString(serverConfig.getAffiliations()));
    }

    public void deleteCaServer(String serverName) {
        Container serverContainer = containerService.selectCAContainer(serverName);
        String hostName = serverContainer.getHostName();
        String containerId = serverContainer.getContainerId();
        try {
            dockerService.deleteContainer(hostName, containerId);
        } finally {
            if (!dockerService.checkContainerExistent(hostName, containerId)) {
                caServerService.deleteServerByName(serverName);
                FileUtils.delete(getLocalServerPath(serverName));
            }
        }
    }

    public DockerService.ChangeContainerStatusResult changeContainerStatus(String serverName, DockerService.ContainerOper oper) {
        Container serverContainer = containerService.selectCAContainer(serverName);
        if (serverContainer == null) throw new ServiceException("不存在名称为" + serverName + "的CA服务");
        String hostName = serverContainer.getHostName();
        String containerId = serverContainer.getContainerId();
        return dockerService.changeContainerStatus(hostName, containerId, oper);
    }

    //-------------------------------------------fabric ca config manage-------------------------------------------------
    public ServerConfigEntity getServerConfig(String serverName, String home, String hostName, String containerId) {
        String serverPath = getLocalServerPath(serverName);
        String remoteFilePath = home + "/" + CONFIG_FILE_NAME;
        dockerService.copyArchiveFromContainer(hostName, containerId, remoteFilePath, serverPath);
        YamlFileUtils fileUtils = new YamlFileUtils();
        try {
            return fileUtils.readYamlFile(serverPath + "/" + CONFIG_FILE_NAME, ServerConfigEntity.class);
        } catch (YamlToPojoException e) {
            LOGGER.error(e);
            throw new ServiceException("CA服务" + serverName + "配置文件读取失败");
        }
    }

    public synchronized ServerConfigEntity setCaServer(String serverName, ServerConfigEntity serverConfig, SetCaServerEntity updateConfig,
                                                       String hostName, String containerId, String home,
                                                       boolean uploadCert, MultipartFile certFile, MultipartFile keyFile) {
        String serverPath = getLocalServerPath(serverName);
        //上传CA证书文件
        String certFileName = null;
        String keyFileName = null;
        if (uploadCert && certFile != null && keyFile != null) {
            MultipartFileUtils fileUtils = new MultipartFileUtils();
            certFileName = "cert.pem";
            String localCertPath = null;
            String localKeyPath = null;
            try {
                String certPem = fileUtils.getFileString(certFile);
                CertFileHelper certHelper = new CertFileHelper();
                keyFileName = certHelper.getFabricPrivateKeyName(certPem);
                localCertPath = fileUtils.saveMultiFile(serverPath, certFile, certFileName);
                localKeyPath = fileUtils.saveMultiFile(serverPath, keyFile, keyFileName);
                //发送CA证书和文件到docker容器的home目录下
                dockerService.copyArchiveToContainer(hostName, containerId, localCertPath, home);
                dockerService.copyArchiveToContainer(hostName, containerId, localKeyPath, home);
            } catch (Exception e) {
                LOGGER.warn(e);
                throw new ContainerConfigException("证书解析失败");
            } finally {
                FileUtils.delete(localCertPath);
                FileUtils.delete(localKeyPath);
            }
        }
        //根据传入参数更新CA的配置文件
        updateConfig(serverConfig, updateConfig, certFileName, keyFileName);
        //把更新后的配置保存在本地文件中
        String resource = serverPath + File.separator + CONFIG_FILE_NAME;
        YamlFileUtils fileUtils = new YamlFileUtils();
        try {
            fileUtils.writeYamlFile(serverConfig, resource);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ServiceException("CA服务配置文件保存失败", e);
        }
        //将本地保存的配置文件拷贝到docker容器中
        dockerService.copyArchiveToContainer(hostName, containerId, resource, home, true);
        try {
            FileUtils.delete(serverPath);
        } catch (RuntimeException e) {
            LOGGER.debug(e);
        }
        //读取配置到本地数据库
        getCaConfig2Db(serverName, hostName, containerId);

        return serverConfig;
    }

    private ServerConfigEntity updateConfig(ServerConfigEntity config, SetCaServerEntity update,
                                            String certFile, String keyFile) {
        if (update == null) throw new IllegalArgumentException("Data to update server config is empty.");

        Integer serverPort = update.getServerPort();
        if (serverPort != null) {
            config.setPort(serverPort);
        }

        String user = update.getUser();
        String userPassword = update.getUserPassword();
        if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(userPassword)) {
            ServerConfigEntity.Registry registry = config.getRegistry();
            if (registry != null) {
                List<ServerConfigEntity.Registry.Identity> identities = registry.getIdentities();
                Map<String, Object> defaultAttrs = new HashMap<>();
                defaultAttrs.put("hf.Registrar.Roles", "*");
                defaultAttrs.put("hf.Registrar.DelegateRoles", "*");
                defaultAttrs.put("hf.Revoker", true);
                defaultAttrs.put("hf.IntermediateCA", true);
                defaultAttrs.put("hf.GenCRL", true);
                defaultAttrs.put("hf.Registrar.Attributes", "*");
                defaultAttrs.put("hf.AffiliationMgr", true);

                ServerConfigEntity.Registry.Identity identity = new ServerConfigEntity.Registry.Identity();
                identity.setName(user);
                identity.setPass(userPassword);
                identity.setType("client");
                identity.setAffiliation("");
                identity.setAttrs(defaultAttrs);

                if (identities != null) {
                    identities.clear();
                } else {
                    identities = new ArrayList<>();
                }
                identities.add(identity);
            }
        }

        Map affiliations = update.getAffiliations();
        if (affiliations != null) {
            config.setAffiliations(affiliations);
        }

        String certExpiry = update.getCertExpiry();
        if (StringUtils.isNotEmpty(certExpiry)) {
            Map signing = config.getSigning();
            if (signing != null && signing.containsKey("default")) {
                Map aDefault = (Map) signing.get("default");
                if (aDefault != null && aDefault.containsKey("expiry")) {
                    aDefault.replace("expiry", certExpiry);
                }
            }
        }

        String cn = update.getCn();
        if (StringUtils.isNotEmpty(cn)) {
            ServerConfigEntity.Csr csr = config.getCsr();
            if (csr != null) {
                csr.setCn(cn);
            }
        }

        String crlExpiry = update.getCrlExpiry();
        if (StringUtils.isNotEmpty(crlExpiry)) {
            ServerConfigEntity.Crl crl = config.getCrl();
            if (crl != null) {
                crl.setExpiry(crlExpiry);
            }
        }

        Map csrName = update.getCsrName();
        if (csrName != null) {
            ServerConfigEntity.Csr csr = config.getCsr();
            if (csr != null) {
                List<Map<String, String>> names = csr.getNames();
                if (names != null) {
                    names.clear();
                } else {
                    names = new ArrayList<>();
                }
                names.add(csrName);
            }
        }

        Boolean debug = update.getDebug();
        if (debug != null) {
            config.setDebug(debug);
        }

        if (certFile != null && keyFile != null) {
            ServerConfigEntity.Ca ca = config.getCa();
            if (ca != null) {
                ca.setCertfile(certFile);
                ca.setKeyfile(keyFile);
            }
        }

        return config;
    }

    public File getCaCert(String serverName, String home, String hostName, String containerId) {
        //从CA的配置文件中读取证书文件存放路径
        ServerConfigEntity serverConfig = getServerConfig(serverName, home, hostName, containerId);
        String certfile = serverConfig.getCa().getCertfile();
        if (!home.endsWith("/")) home = home + "/";
        if (certfile == null || certfile.isEmpty()) {
            certfile = home + "ca-cert.pem";
        } else {
            if (certfile.startsWith("./")) {
                certfile = home + certfile.substring(2);
            } else {
                certfile = home + certfile;
            }
        }
        //从docker容器中获取证书文件保存在本地
        String serverPath = getLocalServerPath(serverName);
        String localFileName = dockerService.copyArchiveFromContainer(hostName, containerId, certfile, serverPath);
        if (localFileName == null || localFileName.isEmpty()) {
            throw new ServiceException("Get cert failed.");
        }
        return new File(localFileName);
    }

    /**
     * 获取本地存放CA配置的目录路径
     */
    private String getLocalServerPath(String serverName) {
        String serverTemp = ExternalResources.getTemp(serverName);
        File serverTempFile = new File(serverTemp);
        if (!serverTempFile.exists()) {
            serverTempFile.mkdirs();
        }
        return serverTemp;
    }


    //----------------------------------------------- parse config data -----------------------------------------------
    private List<FabricCaUser> parseRegistry(ServerConfigEntity.Registry registry) {
        List<FabricCaUser> caUsers = new ArrayList<>();

        int maxEnrollments = registry.getMaxenrollments();
        List<ServerConfigEntity.Registry.Identity> identities = registry.getIdentities();
        if (identities != null) {
            for (ServerConfigEntity.Registry.Identity identity : identities) {
                FabricCaUser caUser = new FabricCaUser();
                caUser.setUserId(identity.getName());
                caUser.setSecret(identity.getPass());
                caUser.setUserType(identity.getType());
                String affiliation = identity.getAffiliation();
                caUser.setAffiliation(affiliation);
                if (StringUtils.isEmpty(affiliation)) {
                    caUser.setIdentityType("admin");
                }
                Attributes attributes = new Attributes(identity.getAttrs());
                caUser.setAttributes(attributes.toString());
                caUser.setMaxEnrollments(maxEnrollments);

                caUsers.add(caUser);
            }
        }

        return caUsers;
    }

    public static class Attributes {
        private List<Attributes.Attr> attributes;

        public Attributes(Map<String, Object> map) {
            this.attributes = new ArrayList<>();
            for (Map.Entry<String, Object> attr : map.entrySet()) {
                this.attributes.add(new Attributes.Attr(attr.getKey(), attr.getValue()));
            }
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for (Attributes.Attr attr : this.attributes) {
                stringBuilder.append(attr.toString());
                stringBuilder.append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        class Attr {
            private String name;
            private String value;

            public Attr(String name, Object value) {
                this.name = name;
                if ((value != null) && (value instanceof Boolean)) {
                    Boolean val = (Boolean) value;
                    if (val) {
                        this.value = "1";
                    } else {
                        this.value = "0";
                    }
                } else if (value != null && value instanceof String) {
                    this.value = (String) value;
                } else if (value != null) {
                    throw new IllegalArgumentException("Unprocessed type : " + value.getClass());
                } else {
                    throw new IllegalArgumentException("The incoming parameter value is empty.");
                }
            }

            @Override
            public String toString() {
                return "{" +
                        "\"name\":\"" + name + "\"" +
                        "," +
                        "\"value\":\"" + value + "\"" +
                        "}";
            }
        }
    }

}
