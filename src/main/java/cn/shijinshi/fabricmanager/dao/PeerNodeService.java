package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.PeerAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.PeerInfo;
import cn.shijinshi.fabricmanager.dao.entity.PeerNode;
import cn.shijinshi.fabricmanager.dao.mapper.PeerNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeerNodeService {

    @Autowired
    private PeerNodeMapper mapper;

    public PeerNode selectByPrimaryKey(String peerName) {
        return mapper.selectByPrimaryKey(peerName);
    }

    public int deleteByPrimaryKey(String peerName) {
        return mapper.deleteByPrimaryKey(peerName);
    }

    public List<PeerAndContainer> selectAllPeer() {
        return mapper.selectAllPeer();
    }

    public int insertSelective(PeerNode peerNode) {
        return mapper.insertSelective(peerNode);
    }

    public PeerInfo getPeerInfo(String peerName) {
        return mapper.selectPeerInfo(peerName);
    }

    public List<PeerInfo> selectAllPeerInfo() {
        return mapper.selectAllPeerInfo();
    }

    public List<String> selectPeerByCaUser(String caUser, String caServerName){
        return mapper.selectPeerByCaUser(caUser, caServerName);
    }

    public List<String> selectAllPeerName(){
        return mapper.selectAllPeerName();
    }
}