package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;

public class VolumeHelper extends DockerHelper{

    public VolumeHelper(DockerClient client) {
        super(client);
    }

    /**
     * 创建指定名称的docker数据卷
     * @param volumeName 数据卷名称
     * @return
     */
    public CreateVolumeResponse createVolume(String volumeName) {
        return client.createVolumeCmd().withName(volumeName).exec();
    }

    /**
     * 创建随机名称的docker数据卷
     * @return
     */
    public CreateVolumeResponse createVolume() {
        return client.createVolumeCmd().exec();
    }

    /**
     * 获取指定数据卷的信息
     * @param volumeName 数据卷名称
     * @return
     */
    public InspectVolumeResponse inspectVolume(String volumeName) {
        return client.inspectVolumeCmd(volumeName).exec();
    }

    /**
     * 移除指定的数据卷
     * @param volumeName 数据卷名称
     */
    public void removeVolume(String volumeName) {
        client.removeVolumeCmd(volumeName).exec();
    }

    /**
     * 获取宿主机上全部数据卷信息
     * @return
     */
    public ListVolumesResponse listVolume() {
        return client.listVolumesCmd().exec();
    }

}
