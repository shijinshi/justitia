package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.peer.CreatePeerEntity;
import cn.shijinshi.fabricmanager.service.DockerService;
import cn.shijinshi.fabricmanager.service.PeerService;
import cn.shijinshi.fabricmanager.service.TokenManager;
import com.github.dockerjava.api.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/node/peer")
public class PeerController {


    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private PeerService service;

    @PostMapping()
    public Response createPeer(@RequestBody @Valid CreatePeerEntity body, HttpServletRequest request) {
        String requester = tokenManager.getRequester(request).getUserId();
        DockerService.ChangeContainerStatusResult result = service.createPeer(requester, body);
        if (result.isSuccess()) {
            return new Response().success("Peer节点创建成功");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }


    @DeleteMapping("/{peerName}")
    public Response deletePeer(@PathVariable("peerName") String peerName) {
        try {
            service.deletePeer(peerName);
        } catch (NotFoundException e) {
            return new Response().failure(e.getMessage());
        }
        return new Response().success("Peer节点" + peerName + "删除成功");
    }

    @PutMapping("/{peerName}/{oper}")
    public Response changeContainerStatus(@PathVariable("peerName") String peerName, @PathVariable("oper") String oper) {
        DockerService.ChangeContainerStatusResult result = service.changeContainerStatus(peerName, DockerService.ContainerOper.fromString(oper));
        if (result.isSuccess()) {
            return new Response().success("Peer节点" + oper + "成功");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @GetMapping()
    public Response getPeers() {
        List<Map> data = service.getLocalPeers();
        return new Response().success(data);
    }
}
