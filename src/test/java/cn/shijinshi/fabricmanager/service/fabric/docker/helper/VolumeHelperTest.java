package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;
import org.junit.Before;
import org.junit.Test;

public class VolumeHelperTest {
    private VolumeHelper volumeHelper;
    private String volumeName = "testVolume";

    @Before
    public void setUp() throws Exception {
//        DockerClientHelper clientHelper = new DockerClientHelper();
//        volumeHelper = clientHelper.getVolumeHelper("testHost");
    }

    @Test
    public void createVolume() {
        CreateVolumeResponse volume = volumeHelper.createVolume(volumeName);
        System.out.println();
    }

    @Test
    public void createVolume1() {
        CreateVolumeResponse volume = volumeHelper.createVolume();
        System.out.println();
    }

    @Test
    public void inspectVolume() {
        InspectVolumeResponse inspectVolumeResponse = volumeHelper.inspectVolume(volumeName);
        System.out.println();
    }

    @Test
    public void removeVolume() {
        volumeHelper.removeVolume(volumeName);
    }

    @Test
    public void listVolume() {
        ListVolumesResponse listVolumesResponse = volumeHelper.listVolume();
        System.out.println();
    }
}