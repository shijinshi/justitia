package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.HostService;
import cn.shijinshi.fabricmanager.dao.OrganizationService;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.Host;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContextServer {
    @Autowired
    private OrganizationService orgService;
    @Autowired
    private HostService hostService;
    @Autowired
    private FabricCaServerService fabricCaService;

    public Map checkSystemConfig() {
        Organization org = orgService.getOrg();
        if (org == null) {
            return formatCheckResult("organization");
        }

        List<Host> hosts = hostService.selectAllHost();
        if (hosts == null || hosts.isEmpty()) {
            return formatCheckResult("host");
        }

        FabricCaServer rootServer = fabricCaService.selectRootServer();
        if (rootServer == null) {
            return formatCheckResult("ca");
        }

       return formatCheckResult(null);
    }

    private Map formatCheckResult(String step) {
        Map<String, Object> status = new HashMap();
        if (step != null) {
            status.put("complete", false);
            status.put("step", step);
        } else {
            status.put("complete", true);
        }
        return status;
    }

    public void resetSystemConfig() {
        //删除全部CA
        //删除全部Peer、orderer
        //删除Org
        orgService.deleteOrganization();
    }
}
