package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import cn.shijinshi.fabricmanager.service.fabric.docker.exception.NetworkManageException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Network;

import java.util.List;

public class NetworkHelper extends DockerHelper{

    public NetworkHelper(DockerClient client) {
        super(client);
    }

    public boolean networkNameExist(String networkName) {
        List<Network> networks = listNetwork();
        if (networks == null || networks.isEmpty()) {
            return false;
        }
        for (Network network : networks) {
            if (networkName.equals(network.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建一个docker网络，使用默认的bridge模式
     *
     * @param networkName 网络名称，同networkMode
     * @return
     */
    public CreateNetworkResponse createNetwork(String networkName) {
        return client.createNetworkCmd()
                .withName(networkName)
                .exec();
    }

    /**
     * 移除指定的docker网络
     *
     * @param networkName
     */
    public void removeNetwork(String networkName) throws NetworkManageException {
        if (networkNameExist(networkName)) {
            client.removeNetworkCmd(networkName).exec();
        }else {
            throw new NetworkManageException("Docker network " + networkName + " does not exists.");
        }

    }

    /**
     * 获取宿主机上所有docker网络
     *
     * @return
     */
    public List<Network> listNetwork() {
        return client.listNetworksCmd().exec();
    }

    /**
     * 查看指定网络的信息
     *
     * @param networkId network id
     * @return
     */
    public Network inspectNetwork(String networkId) throws NotFoundException {
        return client.inspectNetworkCmd()
                .withNetworkId(networkId)
                .exec();
    }

    /**
     * 将指定容器连接到指定network
     *
     * @param networkId   network id
     * @param containerId container id
     */
    public void connectToNetwork(String networkId, String containerId) {
        client.connectToNetworkCmd()
                .withNetworkId(networkId)
                .withContainerId(containerId)
                .exec();
    }

    /**
     * 将指定容器从指定网络移除
     *
     * @param networkId   network id
     * @param containerId container id
     * @param force       是否强制执行
     */
    public void disconnectFromNetwork(String networkId, String containerId, boolean force) {
        client.disconnectFromNetworkCmd()
                .withNetworkId(networkId)
                .withContainerId(containerId)
                .withForce(force)
                .exec();
    }
}
