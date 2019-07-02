package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTask;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ChannelConfigTaskMapper {
    int deleteByPrimaryKey(String requestId);

    int insert(ChannelConfigTask record);

    int insertSelective(ChannelConfigTask record);

    ChannelConfigTask selectByPrimaryKey(String requestId);

    int updateByPrimaryKeySelective(ChannelConfigTask record);

    int updateByPrimaryKeyWithBLOBs(ChannelConfigTask record);

    int updateByPrimaryKey(ChannelConfigTask record);






    List<ChannelConfigTask> selectChannelConfigTask();

    int updateStatus(@Param("requestId") String requestId, @Param("status") String status);

    int updateResponse(@Param("requestId") String requestId, @Param("reject") boolean reject, @Param("reason") String reason, @Param("responseTime") Date responseTime);

    List<ChannelConfigTask> selectMySigningTaskByChannel(@Param("requester") String requester, @Param("channelId") String channelId);
}