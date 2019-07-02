package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.annotation.PassIdentityVerify;
import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.docker.*;
import cn.shijinshi.fabricmanager.service.DockerService;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(value = "/docker")
public class DockerController {
    private final DockerService dockerService;

    @Autowired
    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    //-------------------------------------------- docker container ----------------------------------------------------
    @PassIdentityVerify
    @PostMapping("/container/{hostName}")
    public Response createContainer(@PathVariable("hostName") String hostName, @RequestBody CreateContainerEntity body) {
        body.setHostName(hostName);
        CreateContainerResponse response = dockerService.createContainer(body);
        return new Response().success(response);
    }

    @DeleteMapping("/container/{hostName}/{containerId}")
    public Response deleteContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        dockerService.deleteContainer(hostName, containerId);
        return new Response().success("容器" + containerId + "已删除");
    }

    @PutMapping("/container/start/{hostName}/{containerId}")
    public Response startContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        DockerService.ChangeContainerStatusResult result = dockerService.changeContainerStatus(hostName, containerId, DockerService.ContainerOper.START);
        if (result.isSuccess()) {
            return new Response().success("容器" + containerId + "启动完成");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @PutMapping("/container/pause/{hostName}/{containerId}")
    public Response pauseContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        DockerService.ChangeContainerStatusResult result = dockerService.changeContainerStatus(hostName, containerId, DockerService.ContainerOper.PAUSE);
        if (result.isSuccess()) {
            return new Response().success("容器" + containerId + "已暂停");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @PutMapping("/container/unpause/{hostName}/{containerId}")
    public Response unpauseContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        DockerService.ChangeContainerStatusResult result = dockerService.changeContainerStatus(hostName, containerId, DockerService.ContainerOper.UNPAUSE);
        if (result.isSuccess()) {
            return new Response().success("容器" + containerId + "继续运行");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @PutMapping("/container/restart/{hostName}/{containerId}")
    public Response restartContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        DockerService.ChangeContainerStatusResult result = dockerService.changeContainerStatus(hostName, containerId, DockerService.ContainerOper.RESTART);
        if (result.isSuccess()) {
            return new Response().success("容器" + containerId + "重启完成");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @GetMapping("/container/{hostName}/{containerId}")
    public Response getContainer(@PathVariable("hostName") String hostName, @PathVariable("containerId") String containerId) {
        InspectContainerResponse response = dockerService.getContainer(hostName, containerId);
        return new Response().success(response);
    }

    @GetMapping("/container/{hostName}")
    public Response getContainers(@PathVariable("hostName") String hostName) {
        List<Container> containers = dockerService.getContainers(hostName);
        return new Response().success(containers);
    }


    //-------------------------------------------- docker image ---------------------------------------------------------------
    @PostMapping("/image/{hostName}")
    public Response addImage(@PathVariable("hostName") String hostName, @RequestBody @Valid AddImageEntity body) {
        String imageName = body.getImageName();
        String tag = body.getTag();
        try {
            dockerService.addImage(hostName, imageName, tag);
        } catch (RuntimeException e) {
            return new Response().failure(e.getMessage());
        }
        return new Response().success("镜像" + imageName + "下载完成");
    }

    @DeleteMapping("/image/{hostName}/{imageId}")
    public Response delImage(@PathVariable("hostName") String hostName,@PathVariable("imageId") String imageId) {
        dockerService.deleteImage(hostName, imageId);
        return new Response().success("镜像" + imageId + "已删除");
    }

    @PostMapping("/image/tag/{hostName}")
    public Response tagImage(@PathVariable("hostName") String hostName,@RequestBody @Valid TagImageEntity body) {
        dockerService.tagImage(hostName, body.getImageId(), body.getImageNameWithRepository(), body.getTag());
        return new Response().success("镜像" + body.getImageId() + "成功打上新版本标签"+body.getTag());
    }

    @GetMapping("/image/{hostName}")
    public Response getImages(@PathVariable("hostName") String hostName) {
        List<Image> images = dockerService.listImagesCmd(hostName);
        return new Response().success(images);
    }

//    @GetMapping("/image/{hostName}/{imageName}")
//    public Response getImagesByName(@PathVariable("hostName") String hostName, @PathVariable("imageName") String imageName) {
//        List<Image> images = dockerService.listImagesCmd(hostName, imageName);
//        return new Response().success(images);
//    }


    @GetMapping("/image/{hostName}/{imageId}")
    public Response getImage(@PathVariable("hostName") String hostName,@PathVariable("imageId") String imageId){
        InspectImageResponse image = dockerService.inspectImage(hostName, imageId);
        return new Response().success(image);
    }

    //-------------------------------------------- docker network ---------------------------------------------------------------
    @PostMapping("/network/{hostName}")
    public Response createNetwork(@PathVariable("hostName") String hostName, @RequestBody @Valid CreateNetworkEntity body) {
        CreateNetworkResponse response = dockerService.createNetwork(hostName, body.getNetworkName());
        return new Response().success("Docker网络" + body.getNetworkName() + "添加成功");
    }

    @DeleteMapping("/network/{hostName}/{networkName}")
    public Response deleteNetwork(@PathVariable("hostName") String hostName, @PathVariable("networkName") String networkName) {
        dockerService.removeNetwork(hostName, networkName);
        return new Response().success("Docker网络"+networkName+"已删除");
    }

    @GetMapping("/network/{hostName}")
    public Response getNetworks(@PathVariable("hostName") String hostName) {
        List<Network> networks = dockerService.listNetwork(hostName);
        return new Response().success(networks);
    }

    @GetMapping("/network/{hostName}/{networkName}")
    public Response getNetwork(@PathVariable("hostName") String hostName, @PathVariable("networkName") String networkName) {
        Network network = dockerService.inspectNetwork(hostName, networkName);
        return new Response().success(network);
    }

    //-------------------------------------------- docker volume ---------------------------------------------------------------
    @PostMapping("/volume/{hostName}")
    public Response createVolume(@PathVariable("hostName") String hostName, @RequestBody @Valid CreateVolumeEntity body) {
        CreateVolumeResponse response = dockerService.createVolume(hostName, body.getVolumeName());
        String volumeName = body.getVolumeName() == null ? "": body.getVolumeName();
        return new Response().success("数据卷" +volumeName + "添加成功");
    }

    @DeleteMapping("/volume/{hostName}/{volumeName}")
    public Response deleteVolume(@PathVariable("hostName") String hostName, @PathVariable("volumeName") String volumeName) {
        dockerService.removeVolume(hostName, volumeName);
        return new Response().success("Docker数据卷"+volumeName+"已删除");
    }

    @GetMapping("/volume/{hostName}")
    public Response getVolumes(@PathVariable("hostName") String hostName) {
        ListVolumesResponse response = dockerService.listVolume(hostName);
        return new Response().success(response);
    }

    @GetMapping("/volume/{hostName}/{volumeName}")
    public Response getVolume(@PathVariable("hostName") String hostName, @PathVariable("volumeName") String volumeName) {
        InspectVolumeResponse response = dockerService.inspectVolume(hostName, volumeName);
        return new Response().success(response);
    }
}
