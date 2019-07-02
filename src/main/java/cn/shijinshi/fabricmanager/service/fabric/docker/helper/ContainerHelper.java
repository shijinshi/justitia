package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import cn.shijinshi.fabricmanager.service.fabric.docker.exception.ContainerManageException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ContainerHelper extends DockerHelper {


    public ContainerHelper(DockerClient client) {
        super(client);
    }

    /**
     * @param image 镜像名称，也可以是镜像名称：版本
     * @return 创建容器返回结果，包括容器id和警告信息
     */
    public CreateContainerResponse createContainer(String image, String name, String networkMode, String workingDir, List<String> cmd,
                                                   Map<Integer, Integer> exposedPorts, List<String> env, Map<String, String> volumes, String[] extraHosts) {

        if (StringUtils.isEmpty(image)) throw new IllegalArgumentException("Parameter image is empty.");

        CreateContainerCmd containerCmd = client.createContainerCmd(image);
        //与宿主机相关的配置
        HostConfig hostConfig = new HostConfig();
        if (extraHosts != null && extraHosts.length > 0) {
            hostConfig.withExtraHosts(extraHosts);
        }

        hostConfig.withNetworkMode(networkMode);
        List<Bind> binds = new ArrayList<>();

        //设置容器名称
        if (name != null && !name.isEmpty()) {
            containerCmd.withName(name);
        }
        //容器内工作目录（home）
        if (workingDir != null && !workingDir.isEmpty()) {
            containerCmd.withWorkingDir(workingDir);
        }
        //容器初始执行命令
        if (cmd != null && !cmd.isEmpty()) {
            containerCmd.withCmd(cmd);
        }
        //容器暴露端口
        if (exposedPorts != null && !exposedPorts.isEmpty()) {
            List<ExposedPort> list = new ArrayList<>();
            Ports portBindings = new Ports();
            for (Map.Entry<Integer, Integer> portMap : exposedPorts.entrySet()) {
                Integer key = portMap.getKey();         //暴露出来的端口
                Integer value = portMap.getValue();     //容器内部端口
                ExposedPort port = ExposedPort.tcp(key);
                portBindings.bind(port, Ports.Binding.bindPort(value));
                list.add(port);
            }
            hostConfig.withPortBindings(portBindings);
            containerCmd.withExposedPorts(list);
        }
        //环境变量
        if (env != null && !env.isEmpty()) {
            containerCmd.withEnv(env);
        }
        //挂载卷
        if (volumes != null && !volumes.isEmpty()) {
            List<Volume> list = new ArrayList<>();
            for (Map.Entry<String, String> volume : volumes.entrySet()) {
                String key = volume.getKey();           //宿主机目录挂载地址
                String value = volume.getValue();       //容器内部地址
                Volume volume1 = new Volume(value);
                list.add(volume1);
                binds.add(new Bind(key, volume1));
            }

            containerCmd.withVolumes(list);
        }
        //包含与宿主机相关的配置
        hostConfig.withBinds(binds);
        containerCmd.withHostConfig(hostConfig);
        //创建容器
        return containerCmd.exec();
    }


    /**
     * 返回指定容器状态
     *
     * @param containerId 容器id
     * @return 容器状态(created / exited / running / paused)
     */
    public String getStatus(String containerId) throws ContainerManageException {
        InspectContainerResponse response = inspectContainer(containerId);
        if (response == null) {
            throw new ContainerManageException("Not found docker container by id " + containerId);
        }
        return response.getState().getStatus();
    }


    public String getLog(String containerId, Integer tail) throws InterruptedException {
        LogContainerCmd logContainerCmd = client.logContainerCmd(containerId);
        if (tail != null && tail != 0) {
            logContainerCmd.withTail(tail);
        } else {
            logContainerCmd.withTailAll();
        }
        LogContainerResultCallback callback = logContainerCmd.withStdErr(true).withStdOut(true)
                .exec(new LogContainerResultCallback() {
                    private StringBuilder log = new StringBuilder();

                    @Autowired
                    public void onNext(Frame frame) {
                        log.append(frame.toString() + "\n");
                    }

                    @Autowired
                    public String toString() {
                        return log.toString();
                    }
                });
        callback.awaitCompletion(30, TimeUnit.SECONDS);
        return callback.toString();
    }

    public boolean checkContainerExistent(String containerId) {
        List<Container> containers = listContainer();
        for (Container container : containers) {
            if (containerId.equals(container.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启动容器
     *
     * @param containerId 容器id
     */
    public void startContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("exited".equals(status) || "created".equals(status)) {
            client.startContainerCmd(containerId).exec();
            status = getStatus(containerId);
            if (!"running".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to start.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be started.");
        }
    }

    /**
     * 暂停一个正在运行的容器
     *
     * @param containerId 容器Id
     */
    public void pauseContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("running".equals(status)) {
            client.pauseContainerCmd(containerId).exec();
            status = getStatus(containerId);
            if (!"paused".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to pause.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be paused.");
        }
    }

    /**
     * 启动一个暂停状态的容器
     *
     * @param containerId 容器Id
     */
    public void unpauseContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("paused".equals(status)) {
            client.unpauseContainerCmd(containerId).exec();
            status = getStatus(containerId);
            if (!"running".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to unpause.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be unpaused.");
        }
    }

    /**
     * 停止这个你在运行的容器
     *
     * @param containerId 容器ID
     */
    public void stopContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("running".equals(status)) {
            client.stopContainerCmd(containerId).exec();
            status = getStatus(containerId);
            if (!"exited".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to stop.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be stopped.");
        }
    }

    /**
     * kill正在运行的容器
     *
     * @param containerId 容器ID
     */
    public void killContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("running".equals(status)) {
            client.killContainerCmd(containerId).exec();
            status = getStatus(containerId);
            if (!"exited".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to kill.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be killed.");
        }
    }

    /**
     * 重启正在运行的容器
     *
     * @param containerId 容器ID
     */
    public void restartContainer(String containerId) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("running".equals(status)) {
            client.restartContainerCmd(containerId).exec();
            if (!"running".equals(status)) {
                throw new ContainerManageException("Container number " + containerId + " failed to start.");
            }
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be restarted.");
        }
    }

    /**
     * 移除一反而停止状态的容器
     *
     * @param containerId   容器ID
     * @param force         是否强制执行
     * @param removeVolumes 是否删除容器关联的卷
     */
    public void removeContainer(String containerId, boolean force, boolean removeVolumes) throws ContainerManageException {
        String status = getStatus(containerId);
        if ("exited".equals(status) || "created".equals(status)) {
            client.removeContainerCmd(containerId)
                    .withForce(force)
                    .withRemoveVolumes(removeVolumes)
                    .exec();
        } else {
            throw new ContainerManageException("Status of the Container(" + containerId + ") is " + status + ", the container cannot be removed.");
        }
    }

    /**
     * 获取指定容器状态
     *
     * @param containerId 容器ID
     */
    public InspectContainerResponse inspectContainer(String containerId) throws NotFoundException {
        return client.inspectContainerCmd(containerId).exec();
    }

    /**
     * 获取全部容器信息
     */
    public List<Container> listContainer() {
        return client.listContainersCmd().exec();
    }

}
