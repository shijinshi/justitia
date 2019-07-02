package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTaskResponse;

public interface ChannelConfigTaskResponseMapper {
    int deleteByPrimaryKey(String requestId);

    int insert(ChannelConfigTaskResponse record);

    int insertSelective(ChannelConfigTaskResponse record);

    ChannelConfigTaskResponse selectByPrimaryKey(String requestId);

    int updateByPrimaryKeySelective(ChannelConfigTaskResponse record);

    int updateByPrimaryKeyWithBLOBs(ChannelConfigTaskResponse record);

    int updateByPrimaryKey(ChannelConfigTaskResponse record);
}