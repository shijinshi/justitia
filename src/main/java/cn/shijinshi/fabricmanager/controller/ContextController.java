package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.service.ContextServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/context")
public class ContextController {
    @Autowired
    private ContextServer server;

    @GetMapping("/check")
    public Response checkSystemContext() {
        Map status = server.checkSystemConfig();
        return new Response().success(status);
    }
}
