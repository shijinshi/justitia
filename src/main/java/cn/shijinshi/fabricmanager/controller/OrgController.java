package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.organization.CreateOrgEntity;
import cn.shijinshi.fabricmanager.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/organization")
public class OrgController {

    @Autowired
    private OrgService orgService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response createOrg(CreateOrgEntity form) {
        orgService.createOrg(form);
        return new Response().success("组织"+form.getOrgName()+"被设置");
    }

    @GetMapping
    public Response getOrgInfo(){
        Map orgInfo = orgService.getOrgInfo();
        return new Response().success(orgInfo);
    }
}
