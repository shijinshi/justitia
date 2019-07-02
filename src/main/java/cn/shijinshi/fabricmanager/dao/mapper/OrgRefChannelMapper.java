package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.OrgRefChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrgRefChannelMapper {
    int deleteByPrimaryKey(@Param("channelName") String channelName, @Param("orgMsp") String orgMsp);

    int insert(OrgRefChannel record);

    int insertSelective(OrgRefChannel record);

    OrgRefChannel selectByPrimaryKey(@Param("channelName") String channelName, @Param("orgMsp") String orgMsp);

    int updateByPrimaryKeySelective(OrgRefChannel record);

    int updateByPrimaryKeyWithBLOBs(OrgRefChannel record);




    List<OrgRefChannel> selectAllOrgRefChannel();

    List<OrgRefChannel> selectOrgRefChannelByChannel(@Param("channelName") String channelName);

    int deleteOrgRefChannelByOrg(@Param("orgMsp") String orgMsp);

    int deleteOrgRefChannelByChannel(@Param("channelName") String channelName);
}