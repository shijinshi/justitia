package cn.shijinshi.fabricmanager.service.fabric.channel;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.ChannelConfigTaskResponseService;
import cn.shijinshi.fabricmanager.dao.ChannelConfigTaskService;
import cn.shijinshi.fabricmanager.dao.OrgRefChannelService;
import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTask;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChannelHelper;
import cn.shijinshi.fabricmanager.service.fabric.helper.HFClientHelper;
import cn.shijinshi.fabricmanager.service.fabric.helper.SyncData4Chain;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class MemberManageChaincodeListener implements InitializingBean {
    private static final Logger LOGGER = Logger.getLogger(MemberManageChaincodeListener.class);
    @Value("${fabric.channel.manage.chaincode}")
    private String memberManageChaincodeName;

    private static final HashMap<String, Channel> registeredChannels = new HashMap<>();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final HFClientHelper clientHelper;
    private final ChannelHelper channelHelper;
    private final MemberManageChaincode memberManageChaincode;
    private final ChannelConfigTaskService taskService;
    private final ChannelConfigTaskResponseService taskResponseService;
    private final OrgRefChannelService orgRefChannelService;


    private static HFClient client;

    @Autowired
    public MemberManageChaincodeListener(HFClientHelper clientHelper, ChannelHelper channelHelper,
                                         MemberManageChaincode memberManageChaincode, ChannelConfigTaskService taskService,
                                         ChannelConfigTaskResponseService taskResponseService,
                                         OrgRefChannelService orgRefChannelService) {
        this.clientHelper = clientHelper;
        this.channelHelper = channelHelper;
        this.memberManageChaincode = memberManageChaincode;
        this.taskService = taskService;
        this.taskResponseService = taskResponseService;
        this.orgRefChannelService = orgRefChannelService;
    }

    @Override
    public void afterPropertiesSet() {
        executorService.scheduleAtFixedRate(new CheckListener(), 0, 5, TimeUnit.MINUTES);
    }

    private class CheckListener implements Runnable {
        @Override
        public void run() {
            if (Context.checkContext(true, true, false)) {
                ArrayList<String> channelsName = new ArrayList<>();
                synchronized (registeredChannels) {
                    for (Map.Entry<String, Channel> registeredChannel : registeredChannels.entrySet()) {
                        Channel channel = registeredChannel.getValue();
                        if (channel.isShutdown()) {
                            channelsName.add(registeredChannel.getKey());
                            registeredChannels.remove(registeredChannel.getKey());
                        }
                    }
                }

                for (String channelName : channelsName) {
                    try {
                        registerListener(channelName);
                    } catch (Exception e) {
                        LOGGER.warn("Channel " + channelName + " register " + memberManageChaincodeName + " listener failed.", e);
                    }
                }
            }
        }
    }

    private HFClient getClient() throws FabricClientException {
        if (client == null) {
            client = clientHelper.createHFClient();
        }
        return client;
    }

    /**
     * 获取通道对象
     *
     * @param channelName 通道名称
     * @return channel
     * @throws FabricClientException    Fabric客户端对象创建失败
     * @throws InvalidArgumentException Fabric通道对象创建失败
     * @throws TransactionException     Fabric通道对象初始化失败
     */
    private Channel getChannel(String channelName, Set<String> peersName, long startEvent) throws FabricClientException,
            InvalidArgumentException, TransactionException {
        Channel.PeerOptions options = Channel.PeerOptions.createPeerOptions();
        options.startEvents(startEvent);
        return clientHelper.createChannel(getClient(), channelName, Organization.DEFAULT_ORDERER_NAME, peersName, options);
    }

    public void unregisterListener(List<String> channelsName) {
        synchronized (registeredChannels) {
            for (String channelName : channelsName) {
                if (registeredChannels.containsKey(channelName)) {
                    Channel channel = registeredChannels.get(channelName);
                    if (!channel.isShutdown()) {
                        channel.shutdown(true);
                        registeredChannels.remove(channelName);
                    }
                }
            }
        }
    }

    public boolean registerListener(String channelName) throws Exception {
        synchronized (registeredChannels) {
            if (!registeredChannels.containsKey(channelName)) {
                Set<String> peersName = Context.getPeersName(channelName);
                if (peersName.isEmpty()) {
                    throw new Exception("No Peer node can register to listen");
                }
                Set<String> installedChaincodePeers = new HashSet<>();
                for (String peerName : peersName) {
                    if (checkChaincodeExists(peerName)) {
                        installedChaincodePeers.add(peerName);
                    }
                }
                if (installedChaincodePeers.isEmpty()) {
                    throw new Exception("Chaincode" + memberManageChaincodeName + " is not instantiated on peer nodes");
                }

                long lastConfigBlockNumber;
                try {
                    lastConfigBlockNumber = channelHelper.getConfigBlockNumber(channelName);
                } catch (Exception e) {
                    throw new Exception("Get channel" + channelName + " last config block failed.");
                }

                Channel channel;
                try {
                    channel = getChannel(channelName, installedChaincodePeers, lastConfigBlockNumber);
                } catch (Exception e) {
                    throw new Exception("通道对象创建失败，无法在通道" + channelName + "上创建合约" + memberManageChaincodeName + "的SignRequest事件监听失败。");
                }

                registerSignRequestsListener(channel);
                registerSignResponsesListener(channel);
                registerRequestStateListener(channel);
                registeredChannels.put(channelName, channel);

                getMemberManageTask(channelName);
                return true;
            } else {
                Channel channel = registeredChannels.get(channelName);
                if (channel.isShutdown()) {
                    registeredChannels.remove(channelName);
                    return registerListener(channelName);
                }
                return false;
            }
        }
    }

    /**
     * 获取指定通道当前的任务状态
     *
     * @param channelName 通道名称
     */
    private void getMemberManageTask(String channelName) {
        //获取当前通道配置版本
        byte[] channelConfigurationBytes;
        try {
            channelConfigurationBytes = channelHelper.getChannelConfigurationBytes(channelName);
        } catch (Exception e) {
            LOGGER.error("Channel " + channelName + " config request failed.", e);
            return;
        }
        long sequence;
        try {
            sequence = Configtx.Config.parseFrom(channelConfigurationBytes).getSequence();
        } catch (Exception e) {
            LOGGER.error("Channel " + channelName + " config parse failed.", e);
            return;
        }
        //获取自己发起的（signing状态）签名请求
        List<MemberManageChaincode.SignRequest> requests;
        try {
            requests = memberManageChaincode.getMyRequests(channelName, "signing");
        } catch (MemberManageException e) {
            LOGGER.warn(e);
            return;
        }
        for (MemberManageChaincode.SignRequest request : requests) {
            if (sequence == request.getVersion()) {
                taskService.insertChannelConfigTask(request, channelName);
                //获取签名请求对应的应答
                List<MemberManageChaincode.SignResponse> responses;
                try {
                    responses = memberManageChaincode.getAllSignResponses(channelName, request.getId());
                } catch (MemberManageException e) {
                    LOGGER.warn(e);
                    continue;
                }
                for (MemberManageChaincode.SignResponse response : responses) {
                    taskResponseService.insertTaskResponse(response);
                }
            } else {
                try {
                    memberManageChaincode.updateRequestState(channelName, request.getId(), MemberManageChaincode.RequestState.INVALID);
                } catch (MemberManageException e) {
                    LOGGER.warn(e);
                }
            }
        }
    }

    /**
     * 检查通道成员管理合约合约是否存在
     */
    private boolean checkChaincodeExists(String peerName) {
        //todo 尝试调用这个链码以确保这个链码存在
        return true;
    }

    private void registerSignRequestsListener(Channel channel) {
        try {
            channel.registerChaincodeEventListener(Pattern.compile("^" + memberManageChaincodeName + "$"),
                    Pattern.compile("^" + "SignRequest" + "$"), (handle, blockEvent, chaincodeEvent) -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MemberManageChaincode.SignRequest signRequest;
                        try {
                            signRequest = objectMapper.readValue(chaincodeEvent.getPayload(), MemberManageChaincode.SignRequest.class);
                        } catch (Exception e) {
                            LOGGER.warn("事件SignRequest返回数据解析失败", e);
                            return;
                        }
                        taskService.insertChannelConfigTask(signRequest, channel.getName());
                    });
        } catch (InvalidArgumentException e) {
            LOGGER.error("在通道" + channel.getName() + "上创建合约" + memberManageChaincodeName + "的SignRequest事件监听失败。", e);
        }
    }


    private void registerSignResponsesListener(Channel channel) {
        String orgMspId = Context.getOrganization().getOrgMspId();
        try {
            channel.registerChaincodeEventListener(Pattern.compile("^" + memberManageChaincodeName + "$"),
                    Pattern.compile("^" + "SignResponse_" + orgMspId + "$"), (handle, blockEvent, chaincodeEvent) -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MemberManageChaincode.SignResponse signResponse;
                        try {
                            signResponse = objectMapper.readValue(chaincodeEvent.getPayload(), MemberManageChaincode.SignResponse.class);
                        } catch (IOException e) {
                            LOGGER.warn("事件SignRequest返回数据解析失败", e);
                            return;
                        }
                        taskResponseService.insertTaskResponse(signResponse);
                    });
        } catch (InvalidArgumentException e) {
            LOGGER.error("在通道" + channel.getName() + "上创建合约" + memberManageChaincodeName + "的SignResponses事件监听失败。", e);
        }
    }

    private void registerRequestStateListener(Channel channel) {
        try {
            channel.registerChaincodeEventListener(Pattern.compile("^" + memberManageChaincodeName + "$"),
                    Pattern.compile("^" + "RequestState" + "$"), (handle, blockEvent, chaincodeEvent) -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        RequestState requestState;
                        try {
                            requestState = objectMapper.readValue(chaincodeEvent.getPayload(), RequestState.class);
                        } catch (Exception e) {
                            LOGGER.warn("事件RequestState返回数据解析失败", e);
                            return;
                        }
                        taskService.updateStatus(requestState.getId(), requestState.getState());

                        //检查自已是否存在该通道上的配置请求，如果有就设置为无效
                        if ("end".equals(requestState.getState())) {
                            String channelId = channel.getName();
                            //更新本地数据库中通道和组织关系数据
                            try {
                                orgRefChannelService.deleteOrgRefChannelByChannel(channelId);
                                SyncData4Chain.syncOrgRefChannel();
                            } catch (Exception e) {
                                LOGGER.warn("组织数据更新失败", e);
                            }

                            List<ChannelConfigTask> tasks = taskService.selectMySigningTaskByChannel(channelId);
                            if (tasks != null && !tasks.isEmpty()) {
                                byte[] channelConfigurationBytes;
                                try {
                                    channelConfigurationBytes = channelHelper.getChannelConfigurationBytes(channelId);
                                } catch (Exception e) {
                                    LOGGER.warn("通道" + channelId + "配置数据获取失败");
                                    return;
                                }
                                Configtx.Config config;
                                try {
                                    config = Configtx.Config.parseFrom(channelConfigurationBytes);
                                } catch (Exception e) {
                                    LOGGER.warn("通道" + channelId + "配置数据解析失败");
                                    return;
                                }
                                long configVersion = config.getSequence();
                                for (ChannelConfigTask task : tasks) {
                                    if (configVersion != task.getChannelConfigVersion() && "signing".equals(task.getStatus())) {
                                        try {
                                            memberManageChaincode.updateRequestState(channelId, task.getRequestId(), MemberManageChaincode.RequestState.INVALID);
                                            taskService.updateStatus(task.getRequestId(), "invalid");
                                        } catch (MemberManageException e) {
                                            LOGGER.warn(e);
                                        }
                                    }
                                }
                            }
                        }
                    });
        } catch (InvalidArgumentException e) {
            LOGGER.error("在通道" + channel.getName() + "上创建合约" + memberManageChaincodeName + "的RequestState事件监听失败。", e);
        }
    }


    private static class RequestState {
        private String from;
        private String id;
        private String state;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
