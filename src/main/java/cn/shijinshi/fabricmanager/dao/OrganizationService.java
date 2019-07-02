package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.mapper.OrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationMapper mapper;

    public int insertSelective(Organization org){
        return mapper.insertSelective(org);
    }

    public Organization selectByPrimaryKey(String orgName){
        return mapper.selectByPrimaryKey(orgName);
    }

    public int updateByPrimaryKey(Organization record) {
        return mapper.updateByPrimaryKey(record);
    }

    public List<Organization> selectAllOrg() {
        return mapper.selectAllOrgInfo();
    }

    public Organization getOrg(){
        List<Organization> orgs = mapper.selectAllOrg();
        if (orgs != null  && !orgs.isEmpty()) {
            return orgs.get(0);
        } else {
            return null;
        }
    }

    public int deleteOrganization() {
        return mapper.deleteOrganization();
    }


    public int updateTlsCaServer(Organization organization) {
        return mapper.updateTlsCaServer(organization);
    }
}