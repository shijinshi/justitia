package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.PeerRefChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeerRefChannelMapper {
    int deleteByPrimaryKey(@Param("channelName") String channelName, @Param("peerName") String peerName);

    int insert(PeerRefChannel record);

    int insertSelective(PeerRefChannel record);


    List<PeerRefChannel> selectAllPeerRefChannel();

    List<PeerRefChannel> selectPeerRefChannelBuChannel(@Param("channelName") String channelName);

    List<String> selectAllChannelName();

    int deleteByPeer(@Param("peerName") String peerName);
}