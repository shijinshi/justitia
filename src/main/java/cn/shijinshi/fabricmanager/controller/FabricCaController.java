package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.exception.DownloadFileException;
import cn.shijinshi.fabricmanager.service.FabricCaService;
import cn.shijinshi.fabricmanager.service.TokenManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/ca")
public class FabricCaController {
    private static final Logger LOGGER = Logger.getLogger(FabricCaController.class);
    private final TokenManager tokenManager;
    private final FabricCaService service;

    @Autowired
    public FabricCaController(TokenManager tokenManager, FabricCaService service) {
        this.tokenManager = tokenManager;
        this.service = service;
    }

    /**
     * 获取本地数据库中记录的全部fabric ca server信息
     */
    @GetMapping("/server")
    public Response getCaServers() {
        List<Map<String, Object>> caServers = service.getCaServers();
        return new Response().success(caServers);
    }

    /**
     * 获取请求者有权使用的全部caUser信息
     */
    @GetMapping("/user")
    public Response getCaUsers(HttpServletRequest request) {
        String userId = tokenManager.getRequester(request).getUserId();
        List<Map<String, Object>> caUsers = service.getCaUsers(userId);
        return new Response().success(caUsers);
    }

    /**
     * 获取指定CA用户的全部证书和私钥
     */
    @GetMapping(value = "/cert/{serverName}/{caUserId}")
    public Response getCertByUser(@PathVariable("serverName") String serverName, @PathVariable("caUserId") String caUserId,
                                  HttpServletRequest request) {

        String userId = tokenManager.getRequester(request).getUserId();
        List<Certificates> certs = service.getCertsInfoByUser(caUserId, serverName, userId);
        return new Response().success(certs);
    }

    /**
     * 获取指定CA用户的全部证书和私钥
     */
    @GetMapping(value = "/cert/download/{serverName}/{caUserId}")
    public Object downloadCertByUser(@PathVariable("caUserId") String caUserId, @PathVariable("serverName") String serverName, HttpServletRequest request)
            throws DownloadFileException {

        String userId = tokenManager.getRequester(request).getUserId();
        File file = service.downloadCertByUser(caUserId, serverName, userId);
        ResponseEntity<byte[]> response = DownloadHelper.getResponseEntity(file);
        if (!file.delete()) {
            LOGGER.warn("File delete failed:" + file.getPath());
        }
        return response;
    }

    /**
     * 根据证书序列号和创建者身份获取指定证书
     */
    @GetMapping(value = "/cert/download")
    public ResponseEntity<byte[]> downloadCertBySerial(@RequestParam("serial") String serial, @RequestParam("aki") String aki, HttpServletRequest request)
            throws DownloadFileException {

        String userId = tokenManager.getRequester(request).getUserId();
        File file = service.downloadCertBySerial(serial, aki, userId);
        ResponseEntity<byte[]> response = DownloadHelper.getResponseEntity(file);
        if (!file.delete()) {
            LOGGER.warn("File delete failed:" + file.getPath());
        }
        return response;
    }
}
