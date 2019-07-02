package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.PeerAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.PeerInfo;
import cn.shijinshi.fabricmanager.dao.entity.PeerNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeerNodeMapper {
    int deleteByPrimaryKey(String peerName);

    int insert(PeerNode record);

    int insertSelective(PeerNode record);

    PeerNode selectByPrimaryKey(String peerName);

    int updateByPrimaryKeySelective(PeerNode record);

    int updateByPrimaryKey(PeerNode record);



    List<PeerAndContainer> selectAllPeer();

    PeerInfo selectPeerInfo(@Param("peerName") String peerName);

    List<PeerInfo> selectAllPeerInfo();

    List<String> selectPeerByCaUser(@Param("caPeerUser") String caPeerUser, @Param("caServerName") String caServerName);

    List<String> selectAllPeerName();
}