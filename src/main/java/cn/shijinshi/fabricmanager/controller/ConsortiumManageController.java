package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.consortium.AddOrganizationEntity;
import cn.shijinshi.fabricmanager.controller.entity.consortium.DeleteOrganizationEntity;
import cn.shijinshi.fabricmanager.service.ConsortiumManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/consortium")
public class ConsortiumManageController {

    @Autowired
    private ConsortiumManageService service;

    @GetMapping("/{ordererName}")
    public Response getConsortiums(@PathVariable("ordererName") String ordererName) {
        return new Response().success(service.getConsortiums(ordererName));
    }


    @PostMapping(value = "/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response orgJoinConsortium(AddOrganizationEntity form) {
        String consortiumName = form.getConsortiumName();
        String ordererName = form.getOrdererName();
        String orgName = form.getOrgName();
        MultipartFile orgConfig = form.getOrgConfig();
        service.orgJoinConsortium(ordererName, consortiumName,orgName, orgConfig);
        return new Response().success("新组织成功加入联盟");
    }

    @DeleteMapping("/organization")
    public Response deleteOrgFromConsortium(@RequestBody @Valid DeleteOrganizationEntity body) {
        String ordererName = body.getOrdererName();
        String consortium = body.getConsortium();
        String orgName = body.getOrgName();
        service.deleteOrgFromConsortium(ordererName, consortium, orgName);
        return new Response().success("组织" + orgName + "已从联盟" + consortium + "中移除");
    }
}
