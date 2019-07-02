package cn.shijinshi.fabricmanager.controller;


import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.orderer.CreateOrdererEntity;
import cn.shijinshi.fabricmanager.service.DockerService;
import cn.shijinshi.fabricmanager.service.OrdererService;
import cn.shijinshi.fabricmanager.service.TokenManager;
import com.github.dockerjava.api.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/node/orderer")
public class OrdererController {
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private OrdererService service;

    @PostMapping
    public Response createOrdererConfig(@RequestBody @Valid CreateOrdererEntity body, HttpServletRequest request) {
        String requester = tokenManager.getRequester(request).getUserId();
        DockerService.ChangeContainerStatusResult result = service.createOrderer(requester, body);
        if (result.isSuccess()) {
            return new Response().success("Orderer节点创建成功");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @DeleteMapping("/{ordererName}")
    public Response deleteOrderer(@PathVariable("ordererName") String ordererName) {
        try {
            service.deleteOrderer(ordererName);
        } catch (NotFoundException e) {
            return new Response().failure(e.getMessage());
        }
        return new Response().success("Orderer节点" + ordererName + "删除成功");
    }

    @PutMapping("/{ordererName}/{oper}")
    public Response changeContainerStatus(@PathVariable("ordererName") String ordererName, @PathVariable("oper") String oper) {
        DockerService.ChangeContainerStatusResult result = service.changeContainerStatus(ordererName, DockerService.ContainerOper.fromString(oper));
        if (result.isSuccess()) {
            return new Response().success("Orderer节点" + oper + "成功");
        } else {
            return new Response().failure(result.getErrMsg(), result.getLog());
        }
    }

    @GetMapping()
    public Response getOrderers() {
        List<Map> data = service.getLocalOrderers();
        return new Response().success(data);
    }

}
