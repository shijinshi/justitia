package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.dao.mapper.FabricCaServerMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class FabricCaServerService {

    @Autowired
    private FabricCaServerMapper mapper;
    @Autowired
    private FabricCaUserService caUserService;
    @Autowired
    private CertificatesService certService;

    public FabricCaServer getServer(String serverName) throws NotFoundBySqlException {
        FabricCaServer server = mapper.selectByPrimaryKey(serverName);
        if (server == null) {
            throw new NotFoundBySqlException("No ca server named " + serverName);
        }
        return server;
    }

    public List<FabricCaServer> selectAllServer() {
        return mapper.selectAllServer();
    }

    /**
     * 删除名称为name的记录，以及与name关联的子server
     * @param serverName fabric ca server名称
     * @return 被删除的记录数
     */
    @Transactional
    public int deleteServerByName(String serverName) {
        //删除server下用户的全部证书
        certService.deleteCertByCAServer(serverName);
        //删除此server下的全部用户
        caUserService.deleteByServer(serverName);
        //删除server容器
        mapper.deleteServerContainer(serverName);
        //删除server
        return mapper.deleteByPrimaryKey(serverName);
    }

    public int insertServer(FabricCaServer server) {
        return mapper.insertSelective(server);
    }

    public FabricCaServer selectRootServer(){
        List<FabricCaServer> roots = mapper.selectByType("root");
        if (roots == null || roots.isEmpty()){
            return null;
        } else {
            return roots.get(0);
        }
    }

    public List<FabricCaServer> selectIntermediateServer(){
        return mapper.selectByType("intermediate");
    }

    public List<String> selectCaChildServerName (String serverName) {
        FabricCaServer server = mapper.selectByPrimaryKey(serverName);
        String parentServer = server.getParentServer();
        if (StringUtils.isEmpty(parentServer)) {
            List<String> childrenAndSelf = mapper.selectCAByParent("%");
            childrenAndSelf.remove(serverName);
            return childrenAndSelf;
        } else {
            return mapper.selectCAByParent(parentServer + ".%");
        }

    }

    public int updateAffiliations(String serverName, String affiliations) {
        return mapper.updateAffiliations(serverName, affiliations);
    }
}