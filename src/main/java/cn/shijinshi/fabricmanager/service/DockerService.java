package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;
import cn.shijinshi.fabricmanager.dao.ContainerService;
import cn.shijinshi.fabricmanager.dao.HostService;
import cn.shijinshi.fabricmanager.dao.entity.Host;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.docker.exception.ContainerManageException;
import cn.shijinshi.fabricmanager.service.fabric.docker.exception.ImageManageException;
import cn.shijinshi.fabricmanager.service.fabric.docker.exception.NetworkManageException;
import cn.shijinshi.fabricmanager.service.fabric.docker.helper.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class DockerService {
    private static final Logger LOGGER = Logger.getLogger(DockerService.class);
    @Autowired
    private ContainerService containerService;
    @Autowired
    private HostService hostService;

    public DockerClient getClient(String hostName) {
        Host host;
        try {
            host = hostService.getHost(hostName);
        } catch (NotFoundBySqlException e) {
            LOGGER.debug(e);
            throw new ServiceException("无法连接到Docker服务，不存在名称为" + hostName + "的主机");
        }
        return createClient(host);
    }

    private DockerClient createClient(Host host) {
        Boolean tlsEnable = host.getTlsEnable();
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host.getProtocol() + "://" + host.getIp() + ":" + host.getPort())
                .withDockerTlsVerify(tlsEnable);

        if (tlsEnable) {
            builder.withDockerCertPath(host.getCertPath());
        }

        return DockerClientBuilder.getInstance(builder.build()).build();
    }

    public boolean testLink(Host host) {
        DockerClient client = createClient(host);
        try {
            client.infoCmd().exec();
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("无法连接到主机" + host.getHostName() + "请确认主机配置参数是否正确", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                LOGGER.warn(e);
                return false;
            }
        }
        return true;
    }

    //-------------------------------------------- docker cmd ----------------------------------------------------------
    public static class ExecCmdBean {
        private String workingDir;
        private String userName;
        private List<String> envs;
        private String cmd;
        private Boolean detach;
        private String input;
        private long timeout;

        public String getWorkingDir() {
            return workingDir;
        }

        public void setWorkingDir(String workingDir) {
            this.workingDir = workingDir;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public List<String> getEnvs() {
            return envs;
        }

        public void setEnvs(List<String> envs) {
            this.envs = envs;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public Boolean getDetach() {
            return detach;
        }

        public void setDetach(Boolean detach) {
            this.detach = detach;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }

    public CmdHelper.Result execCmd(String hostName, String containerId, ExecCmdBean cmd) {
        CmdHelper cmdHelper = new CmdHelper(getClient(hostName));
        try {
            ExecCreateCmdResponse response = cmdHelper.createCmd(containerId, cmd.getWorkingDir(), cmd.getUserName(), cmd.getEnvs(), cmd.getCmd().split("\\s+"));
            return cmdHelper.startCmd(response.getId(), cmd.getDetach(), cmd.getInput(), cmd.getTimeout());
        } finally {
            cmdHelper.close();
        }
    }

    //-------------------------------------------- docker container ----------------------------------------------------
    public CreateContainerResponse createContainer(CreateContainerEntity config) {
        String hostName = config.getHostName();
        String imageName = config.getImage();
        String tag = config.getTag();
        String image = imageName + ":" + tag;
        //检查镜像是否存在
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        boolean imageExist;
        try {
            imageExist = imageHelper.imageExist(imageName, tag);
        } finally {
            imageHelper.close();
        }
        if (!imageExist) {
            throw new ServiceException("远程主机" + hostName + "上不存在镜像" + image + "请先下载相关镜像");
        }

        //检查容器网络是否存在，如果不存在则创建
        String networkMode = config.getNetworkMode();
        NetworkHelper networkHelper = new NetworkHelper(getClient(hostName));
        if (!networkHelper.networkNameExist(networkMode)) {
            networkHelper.createNetwork(networkMode);
        }

        //创建容器，但是不启动
        List<String> cmd = null;
        if (StringUtils.isNotEmpty(config.getCmd())) {
            String[] cmdArr = config.getCmd().split("\\s+");
            cmd = Arrays.asList(cmdArr);
        }
        List<String> env = new ArrayList<>();
        Map<String, String> envs = config.getEnv();
        if (envs != null && !envs.isEmpty()) {
            for (Map.Entry<String, String> entry : config.getEnv().entrySet()) {
                env.add(entry.getKey() + "=" + entry.getValue());
            }
        }

        String containerName = config.getContainerName();
        String workingDir = config.getWorkingDir();
        Map<Integer, Integer> exposedPorts = config.getExposedPorts();
        Map<String, String> volumes = config.getVolumes();
        String[] extraHosts = config.getExtraHosts();
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        CreateContainerResponse response;
        try {
            response = containerHelper.createContainer(image, containerName, networkMode,
                    workingDir, cmd, exposedPorts, env, volumes, extraHosts);
        } finally {
            containerHelper.close();
        }

        //保存到数据库
        cn.shijinshi.fabricmanager.dao.entity.Container container = new cn.shijinshi.fabricmanager.dao.entity.Container();
        container.setHostName(hostName);
        container.setContainerId(response.getId());
        container.setContainerName(containerName);
        container.setImage(imageName);
        container.setTag(tag);
        container.setWorkingDir(workingDir);
        container.setNetworkMode(networkMode);
        container.setVolumes(JSONObject.valueToString(volumes));
        container.setExposedPort(JSONObject.valueToString(exposedPorts));
        container.setStatus("created");
        containerService.insertContainer(container);

        return response;
    }

    public enum ContainerOper {
        START("start"),
        RESTART("restart"),
        PAUSE("pause"),
        UNPAUSE("unpause"),
        STOP("stop");

        private final static Map<String, ContainerOper> ENUM_MAP = new HashMap<>();

        static {
            for (ContainerOper v : values()) {
                ENUM_MAP.put(v.getOper(), v);
            }
        }

        public static ContainerOper fromString(String oper) {
            ContainerOper res = ENUM_MAP.get(oper);
            if (res == null) {
                throw new ServiceException("未知的容器操作(" + oper + ")");
            }
            return res;
        }

        private String oper;

        ContainerOper(String oper) {
            this.oper = oper;
        }

        public String getOper() {
            return oper;
        }
    }

    public boolean checkContainerExistent(String hostName, String containerId) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            return containerHelper.checkContainerExistent(containerId);
        }finally {
            containerHelper.close();
        }
    }

    public String getStatus(String hostName, String containerId) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            return containerHelper.getStatus(containerId);
        } catch (ContainerManageException e) {
            LOGGER.error(e);
            throw new ServiceException("获取容器状态失败", e);
        } finally {
            containerHelper.close();
        }
    }

    /**
     * 获取指定容器日志
     *
     * @param hostName    容器所在主机名称
     * @param containerId 容器ID
     * @param tail        最近的日志条数，如果tail为null或0则获取全部日志
     * @return 日志信息
     */
    public String getLog(String hostName, String containerId, Integer tail) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            return containerHelper.getLog(containerId, tail);
        } catch (InterruptedException e) {
            LOGGER.error(e);
            throw new ServiceException("获取容器日志失败", e);
        } finally {
            containerHelper.close();
        }
    }

    public class ChangeContainerStatusResult {
        private boolean success;
        private String errMsg;
        private String finalStatus;
        private String log;

        public ChangeContainerStatusResult(boolean success, String errMsg, String finalStatus, String log) {
            this.success = success;
            this.errMsg = errMsg;
            this.finalStatus = finalStatus;
            this.log = log;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public String getLog() {
            return log;
        }
    }

    public ChangeContainerStatusResult changeContainerStatus(String hostName, String containerId, ContainerOper oper) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        boolean success = true;
        String errMsg = null;
        String status = null;
        String log = null;
        String expectedStatus;
        try {
            switch (oper) {
                case START:
                    containerHelper.startContainer(containerId);
                    expectedStatus = "running";
                    break;
                case RESTART:
                    containerHelper.restartContainer(containerId);
                    expectedStatus = "running";
                    break;
                case PAUSE:
                    containerHelper.pauseContainer(containerId);
                    expectedStatus = "paused";
                    break;
                case UNPAUSE:
                    containerHelper.unpauseContainer(containerId);
                    expectedStatus = "running";
                    break;
                case STOP:
                    containerHelper.stopContainer(containerId);
                    expectedStatus = "stopped";
                    break;
                default:
                    success = false;
                    errMsg = "Operation(" + oper + ") type is not supported.";
                    return new ChangeContainerStatusResult(success, errMsg, status, log);
            }

            Thread.sleep(10000);        //让容器跑一会再查看
            status = containerHelper.getStatus(containerId);
            if (!expectedStatus.equals(status)) {
                success = false;
                errMsg = "Container(" + containerId + ") start operation failed, current container status is " + status;
                try {
                    log = containerHelper.getLog(containerId, 200);
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            success = false;
            errMsg = e.getMessage();
        } finally {
            containerHelper.close();
        }
        return new ChangeContainerStatusResult(success, errMsg, status, log);
    }

    public void deleteContainer(String hostName, String containerId) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            containerHelper.stopContainer(containerId);
        } catch (ContainerManageException e) {
            LOGGER.debug(e);
        } finally {
            try {
                containerHelper.removeContainer(containerId, true, true);
                containerService.deleteContainer(hostName, containerId);
            } catch (ContainerManageException e) {
                LOGGER.error(e);
                throw new ServiceException("删除容器(" + containerId + "失败", e);
            } finally {
                containerHelper.close();
            }
        }
    }

    public InspectContainerResponse getContainer(String hostName, String containerId) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            return containerHelper.inspectContainer(containerId);
        } finally {
            containerHelper.close();
        }
    }

    public List<Container> getContainers(String hostName) {
        ContainerHelper containerHelper = new ContainerHelper(getClient(hostName));
        try {
            return containerHelper.listContainer();
        } finally {
            containerHelper.close();
        }
    }

    public void copyArchiveToContainer(String hostName, String containerId, String resource, String remotePath) {
        copyArchiveToContainer(hostName, containerId, resource, remotePath, false);
    }

    public void copyArchiveToContainer(String hostName, String containerId, String resource, String remotePath, boolean overwrite) {
        ArchiveHelper archiveHelper = new ArchiveHelper(getClient(hostName));
        try {
            archiveHelper.copyArchiveToContainer(containerId, resource, remotePath, overwrite);
        } finally {
            archiveHelper.close();
        }
    }

    public String copyArchiveFromContainer(String hostName, String containerId, String resource, String savePath) {
        ArchiveHelper archiveHelper = new ArchiveHelper(getClient(hostName));
        try {
            return archiveHelper.copyArchiveFromContainer(containerId, resource, savePath);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new ServiceException("复制文件到容器中失败", e);
        } finally {
            archiveHelper.close();
        }
    }

    //-------------------------------------------- docker image --------------------------------------------------------
    public void addImage(String hostName, String imageName, String tag) {
        if (StringUtils.isEmpty(tag)) {
            tag = "latest";
        }
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            imageHelper.pullImage(imageName, tag);
        } catch (InterruptedException | ImageManageException e) {
            LOGGER.error(e);
            throw new ServiceException("为主机" + hostName +"下载镜像(" + imageName + ":" + tag + ")失败", e);
        }  finally {
            imageHelper.close();
        }
    }

    public void deleteImage(String hostName, String imageId) {
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            imageHelper.removeImage(imageId);
        } catch (ImageManageException e) {
            LOGGER.error(e);
            throw new ServiceException("删除主机" + hostName + "上的镜像(" + imageId + ")失败", e);
        } finally {
            imageHelper.close();
        }
    }

    public void tagImage(String hostName, String imageId, String imageNameWithRepository, String tag) {
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            imageHelper.tagImage(imageId, imageNameWithRepository, tag);
        } finally {
            imageHelper.close();
        }
    }

    public InspectImageResponse inspectImage(String hostName, String imageId) {
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            return imageHelper.inspectImage(imageId);
        } finally {
            imageHelper.close();
        }
    }

    public List<Image> listImagesCmd(String hostName) {
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            return imageHelper.listImagesCmd();
        } finally {
            imageHelper.close();
        }
    }

    public List<Image> listImagesCmd(String hostName, String imageName) {
        ImageHelper imageHelper = new ImageHelper(getClient(hostName));
        try {
            return imageHelper.listImagesCmd(imageName);
        } finally {
            imageHelper.close();
        }
    }

    //-------------------------------------------- docker network ------------------------------------------------------
    public CreateNetworkResponse createNetwork(String hostName, String networkName) {
        NetworkHelper networkHelper = new NetworkHelper(getClient(hostName));
        try {
            if(!networkHelper.networkNameExist(networkName)) {
                return networkHelper.createNetwork(networkName);
            } else {
                throw new ServiceException("主机" +hostName +"上已存在名为" + networkName + "的docker网络");
            }
        } finally {
            networkHelper.close();
        }
    }

    public void removeNetwork(String hostName, String networkName) {
        NetworkHelper networkHelper = new NetworkHelper(getClient(hostName));
        try {
            networkHelper.removeNetwork(networkName);
        } catch (NetworkManageException e) {
            LOGGER.error(e);
            throw new ServiceException("删除主机"+ hostName + "上的docker网络(" + networkName +")失败", e);
        } finally {
            networkHelper.close();
        }
    }

    public List<Network> listNetwork(String hostName) {
        NetworkHelper networkHelper = new NetworkHelper(getClient(hostName));
        try {
            return networkHelper.listNetwork();
        } finally {
            networkHelper.close();
        }
    }

    public Network inspectNetwork(String hostName, String networkId) {
        NetworkHelper networkHelper = new NetworkHelper(getClient(hostName));
        try {
            return networkHelper.inspectNetwork(networkId);
        } finally {
            networkHelper.close();
        }
    }

    //-------------------------------------------- docker volume -------------------------------------------------------
    public CreateVolumeResponse createVolume(String hostName, String volumeName) {
        VolumeHelper volumeHelper = new VolumeHelper(getClient(hostName));
        try {
            if (volumeName == null || volumeName.isEmpty()) {
                return volumeHelper.createVolume();
            } else {
                return volumeHelper.createVolume(volumeName);
            }
        } finally {
            volumeHelper.close();
        }
    }

    public void removeVolume(String hostName, String volumeName) {
        VolumeHelper volumeHelper = new VolumeHelper(getClient(hostName));
        try {
            volumeHelper.removeVolume(volumeName);
        } finally {
            volumeHelper.close();
        }
    }

    public InspectVolumeResponse inspectVolume(String hostName, String volumeName) {
        VolumeHelper volumeHelper = new VolumeHelper(getClient(hostName));
        try {
            return volumeHelper.inspectVolume(volumeName);
        } finally {
            volumeHelper.close();
        }
    }

    public ListVolumesResponse listVolume(String hostName) {
        VolumeHelper volumeHelper = new VolumeHelper(getClient(hostName));
        try {
            return volumeHelper.listVolume();
        } finally {
            volumeHelper.close();
        }
    }
}
