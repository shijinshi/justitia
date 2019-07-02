package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.PeerRefChannel;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManageChaincodeListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PeerRefChannelService {
    private static final Logger LOGGER = Logger.getLogger(PeerRefChannelService.class);
    private static final HashSet<PeerRefChannel> peerRefChannelSet = new HashSet<>();

    private final MemberManageChaincodeListener memberManageChaincodeListener;

    @Autowired
    public PeerRefChannelService(MemberManageChaincodeListener memberManageChaincodeListener) {
        this.memberManageChaincodeListener = memberManageChaincodeListener;
    }

    public void updatePeerRefChannel(Map<String, Set<String>> peerRefChannels) {
        List<String> channelsNameOld = selectAllChannelName();
        synchronized (peerRefChannelSet) {
            peerRefChannelSet.clear();
            for (Map.Entry<String, Set<String>> entry : peerRefChannels.entrySet()) {
                String peerName = entry.getKey();
                Set<String> channelsName = entry.getValue();
                for (String channelName : channelsName) {
                    PeerRefChannel peerRefChannel = new PeerRefChannel();
                    peerRefChannel.setPeerName(peerName);
                    peerRefChannel.setChannelName(channelName);
                    peerRefChannelSet.add(peerRefChannel);
                }
            }
        }
        List<String> channelsNameNew = selectAllChannelName();
        for (String channelName : channelsNameNew) {
            channelsNameOld.remove(channelName);
            try {
                memberManageChaincodeListener.registerListener(channelName);
            } catch (Exception e) {
                LOGGER.warn("Channel " + channelName + " register mmcc listener failed.", e);
            }
        }

        memberManageChaincodeListener.unregisterListener(channelsNameOld);
    }

    public int insertSelective(PeerRefChannel peerRefChannel) {
        synchronized (peerRefChannelSet) {
            if (peerRefChannelSet.add(peerRefChannel)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public List<PeerRefChannel> selectAllPeerRefChannel() {
        synchronized (peerRefChannelSet) {
            return new ArrayList<>(peerRefChannelSet);
        }
    }

    public List<PeerRefChannel> selectPeerRefChannelBuChannel(String channelName) {
        if (null == channelName || channelName.isEmpty()) {
            throw new IllegalArgumentException("Channel name is empty.");
        }
        ArrayList<PeerRefChannel> peerRefChannels = new ArrayList<>();
        synchronized (peerRefChannelSet) {
            for (PeerRefChannel peerRefChannel : peerRefChannelSet) {
                if (channelName.equals(peerRefChannel.getChannelName())) {
                    peerRefChannels.add(peerRefChannel);
                }
            }
        }
        return peerRefChannels;
    }

    public List<String> selectAllChannelName() {
        HashSet<String> channelsName = new HashSet<>();
        synchronized (peerRefChannelSet) {
            for (PeerRefChannel peerRefChannel : peerRefChannelSet) {
                channelsName.add(peerRefChannel.getChannelName());
            }
        }
        return new ArrayList<>(channelsName);
    }

    public void deleteByPeer(String peerName) {
        if (null == peerName || peerName.isEmpty()) {
            throw new IllegalArgumentException("Peer name is empty.");
        }

        synchronized (peerRefChannelSet) {
            for (PeerRefChannel peerRefChannel : peerRefChannelSet) {
                if (peerName.equals(peerRefChannel.getPeerName())) {
                    peerRefChannelSet.remove(peerRefChannel);
                }
            }
        }
    }

    public HashSet<String> selectPeersNameByChannel(String channelName) {
        if (null == channelName || channelName.isEmpty()) {
            throw new IllegalArgumentException("Channel name is empty.");
        }
        HashSet<String> peersName = new HashSet<>();
        synchronized (peerRefChannelSet) {
            for (PeerRefChannel peerRefChannel : peerRefChannelSet) {
                if (channelName.equals(peerRefChannel.getChannelName())) {
                    peersName.add(peerRefChannel.getPeerName());
                }
            }
        }
        return peersName;
    }
}
