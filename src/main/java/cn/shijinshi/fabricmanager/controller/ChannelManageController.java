package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.channel.*;
import cn.shijinshi.fabricmanager.exception.DownloadFileException;
import cn.shijinshi.fabricmanager.service.ChannelManageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/channel")
public class ChannelManageController {
    private static final Logger LOGGER = Logger.getLogger(ChannelManageController.class);
    private final ChannelManageService service;

    @Autowired
    public ChannelManageController(ChannelManageService service) {
        this.service = service;
    }


    /**
     * 获取通道信息
     */
    @GetMapping
    public Response getChannels() {
        return new Response().success(service.getChannelsInfo());
    }

    /**
     * 获取指定通道信息
     */
    @GetMapping("/{channelName}")
    public Response getChannel(@PathVariable("channelName") String channelName) {
        return new Response().success(service.getChannelInfo(channelName));
    }

    /**
     * 创建通道
     */
    @PostMapping
    public Response createChannel(@RequestBody @Valid CreateChannelEntity body) {
        String channelName = body.getChannelName();
        String consortiumName = body.getConsortiumName();
        service.createChannel(channelName, consortiumName);
        return new Response().success("通道" + channelName + "创建成功");
    }

    /**
     * Peer节点加入通道
     */
    @PostMapping("/join")
    public Response joinChannel(@RequestBody @Valid JoinChannelEntity body) {
        String channelName = body.getChannelName();
        String peerName = body.getPeerName();
        service.peerJoinChannel(channelName, peerName);
        return new Response().success("节点" + peerName + "成功加入通道" + channelName);
    }

    /*
     * 获取本组织的配置信息
     */
    @GetMapping("/organization/config")
    public Object getOrganizationConfig() {
        File orgConfig = service.getOrgConfig();
        ResponseEntity<byte[]> responseEntity;
        try {
            responseEntity = DownloadHelper.getResponseEntity(orgConfig);
        } catch (DownloadFileException e) {
            return new Response().failure(e.getMessage());
        }
        if(!orgConfig.getParentFile().delete()){
            LOGGER.warn("File delete failed:" + orgConfig.getPath());
        }
        return responseEntity;
    }

    /**
     * 增加组织，组织介绍者身份
     */
    @PostMapping(value = "/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response addOrganization(AddOrganizationEntity form) {
        String channelName = form.getChannelName();
        String description = form.getDescription();
        String orgName = form.getOrgName();
        MultipartFile orgConfig = form.getOrgConfig();
        service.addOrganization(channelName,orgName, orgConfig, description);
        return new Response().success("增加组织请求创建成功");
    }

    /**
     * 获取指定组织的全部MSPID
     */
    @GetMapping("/msp/{channelName}")
    public Response getChannelMspIds(@PathVariable("channelName") String channelName) {
        return new Response().success(service.getChannelMspId(channelName));
    }

    /**
     * 删除组织
     */
    @DeleteMapping("/organization")
    public Response deleteOrganization(@RequestBody DeleteOrganizationEntity body) {
        String channelName = body.getChannelName();
        String orgName = body.getOrgName();
        String description = body.getDescription();
        service.deleteOrganization(channelName, orgName, description);
        return new Response().success("组织" + orgName + "删除请求创建成功");
    }

    /**
     * 获取全部待处理任务
     */
    @GetMapping("/task")
    public Response getChannelConfigTask() {
        List<Map> tasks = service.getTasks();
        return new Response().success(tasks);
    }

    /**
     * 获取指定待处理任务的详细信息
     */
    @GetMapping("/task/{taskId}")
    public Response getChannelConfigTask(@PathVariable("taskId") String taskId) {
        return new Response().success(service.getTask(taskId));
    }

    /**
     * 任务响应
     */
    @PostMapping("/task/response")
    public Response channelConfigTaskResponse(@RequestBody @Valid ResponseTaskEntity body) {
        String taskId = body.getTaskId();
        Boolean reject = body.isReject();
        service.channelConfigTaskResponse(taskId, reject, body.getReason());
        if (reject) {
            return new Response().success("请求" + taskId + "已拒绝");
        } else {
            return new Response().success("请求" + taskId + "已接受");
        }
    }

    @PutMapping("/task/submit/{taskId}")
    public Response submitRequest(@PathVariable("taskId") String taskId) {
        service.submitRequest(taskId);
        return new Response().success("请求" + taskId + "以生效");
    }

    @PutMapping("/task/recall/{taskId}")
    public Response recallRequest(@PathVariable("taskId") String taskId) {
        service.recallMyRequest(taskId);
        return new Response().success("请求" + taskId +"已撤销");
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/task/{taskId}")
    public Response deleteChannelConfigTask(@PathVariable("taskId") String taskId) {
        service.deleteTask(taskId);
        return new Response().success("请求" + taskId + "已删除");
    }

    /**
     * 增加锚节点
     */
    @PostMapping("/anchor")
    public Response addAnchorPeer() {
        //TODO 增加锚节点，参考fabric-sdk-java的UpdateChannelIT.java(266行左右)
        return new Response().success();
    }

    /**
     * 删除锚节点
     */
    @DeleteMapping("/anchor")
    public Response deleteAnchorPeer() {
        return new Response().success();
    }
}
