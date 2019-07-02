package cn.shijinshi.fabricmanager.service.fabric.helper;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.OrgRefChannelService;
import cn.shijinshi.fabricmanager.dao.PeerRefChannelService;
import cn.shijinshi.fabricmanager.dao.entity.OrgRefChannel;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.hyperledger.fabric.protos.peer.Configuration;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SyncData4Chain {
    private static final Logger LOGGER = Logger.getLogger(SyncData4Chain.class);
    private static ChannelHelper channelHelper;
    private static PeerRefChannelService peerRefChannelService;
    private static OrgRefChannelService orgRefChannelService;

    private static Boolean syncing = false;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Autowired
    public SyncData4Chain(ChannelHelper channelHelper, PeerRefChannelService peerRefChannelService,
                          OrgRefChannelService orgRefChannelService) {
        SyncData4Chain.channelHelper = channelHelper;
        SyncData4Chain.peerRefChannelService = peerRefChannelService;
        SyncData4Chain.orgRefChannelService = orgRefChannelService;
    }

    public void begin() {
        if (!syncing && Context.checkContext(true, true, true)) {
            //定期从链上同步一些数据
            executorService.scheduleAtFixedRate(new SyncData(), 0, 5, TimeUnit.MINUTES);
            syncing = true;
        }
    }

    public void stop() {
        if (syncing) {
            syncing = false;
        }
    }

    private class SyncData implements Runnable {
        @Override
        public void run() {
            try {
                if (syncing && Context.checkContext(true, true, false)) {
                    //peer加入通道时，调用系统合约CSCC不会产生交易，所以不能通过监听的方式感知peer和channel的关联关系
                    syncPeerRefChannel();
                    //fixme 可以改成当出现通道配置交易时，就去同步最新的组织和通道对应数据
                    syncOrgRefChannel();
                }
            } catch (Exception e) {
                LOGGER.warn("Sysnc data from chain failed.", e);
            }
        }
    }

    /**
     * 1、同步peer和channel的关系
     * 2、发现存活的peer节点
     */
    public static synchronized void syncPeerRefChannel() {
        Set<String> peersName = Context.getPeersName();
        Map<String, Set<String>> peerRefChannels = new HashMap<>();
        if (!peersName.isEmpty()) {
            for (String peerName : peersName) {
                Set<String> channelsName;
                try {
                    channelsName = channelHelper.queryChannels(peerName);
                } catch (InvalidArgumentException | ProposalException e) {
                    LOGGER.debug("get channel list failed." + e.getMessage());
                    continue;
                } catch (FabricClientException e) {
                    LOGGER.debug("fabric request client create failed." + e.getMessage());
                    continue;
                }
                peerRefChannels.put(peerName, channelsName);
            }

            peerRefChannelService.updatePeerRefChannel(peerRefChannels);
        }
    }

    public static synchronized void syncOrgRefChannel() {
        List<String> channelsName = peerRefChannelService.selectAllChannelName();
        if (channelsName != null && !channelsName.isEmpty()) {
            for (String channelName : channelsName) {
                Configtx.Config config;
                try {
                    //获取通道配置
                    byte[] channelConfigBytes = channelHelper.getChannelConfigurationBytes(channelName);
                    config = Configtx.Config.parseFrom(channelConfigBytes);
                } catch (TransactionException | InvalidProtocolBufferException | InvalidArgumentException e) {
                    LOGGER.warn("get channel(" +channelName +") config failed." +e.getMessage());
                    continue;
                } catch (FabricClientException e) {
                    LOGGER.error("fabric request client create failed." + e.getMessage());
                    continue;
                }

                Configtx.ConfigGroup application = config.getChannelGroup().getGroupsMap().get("Application");
                Map<String, Configtx.ConfigGroup> groupsMap = application.getGroupsMap();
                for (Map.Entry<String, Configtx.ConfigGroup> group : groupsMap.entrySet()) {
                    OrgRefChannel orgRefChannel = new OrgRefChannel();
                    orgRefChannel.setChannelName(channelName);
                    orgRefChannel.setOrgMsp(group.getKey());
                    Configtx.ConfigGroup configGroup = group.getValue();
                    Map<String, Configtx.ConfigValue> valuesMap = configGroup.getValuesMap();
                    if (valuesMap.containsKey("AnchorPeers")) {
                        Configtx.ConfigValue anchorPeersConfig = valuesMap.get("AnchorPeers");
                        List<Configuration.AnchorPeer> anchorPeersList;
                        try {
                            Configuration.AnchorPeers anchorPeers = Configuration.AnchorPeers.parseFrom(anchorPeersConfig.getValue());
                            anchorPeersList = anchorPeers.getAnchorPeersList();
                        } catch (InvalidProtocolBufferException e) {
                            LOGGER.warn("anchor peer info parse failed." + e.getMessage());
                            break;
                        }
                        List<String> anchorPeersStr = new ArrayList<>();
                        if (anchorPeersList != null && !anchorPeersList.isEmpty()) {
                            for (Configuration.AnchorPeer anchorPeer : anchorPeersList) {
                                anchorPeersStr.add(anchorPeer.getHost() + ":" + anchorPeer.getPort());
                            }
                        }
                        orgRefChannel.setAnchorPeers(JSONObject.valueToString(anchorPeersStr));
                    }
                    orgRefChannelService.insertSelective(orgRefChannel);
                }
            }
        }
    }
}
