package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Organization;

import java.util.List;

public interface OrganizationMapper {
    int deleteByPrimaryKey(String orgName);

    int insert(Organization record);

    int insertSelective(Organization record);

    Organization selectByPrimaryKey(String orgName);

    int updateByPrimaryKeySelective(Organization record);

    int updateByPrimaryKeyWithBLOBs(Organization record);

    int updateByPrimaryKey(Organization record);






    List<Organization> selectAllOrg();

    /**
     * 获取除证书以外的信息
     */
    List<Organization> selectAllOrgInfo();

    int deleteOrganization();

    int updateTlsCaServer(Organization organization);
}