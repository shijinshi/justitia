package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.dao.ChannelConfigTaskService;
import cn.shijinshi.fabricmanager.dao.OrgRefChannelService;
import cn.shijinshi.fabricmanager.dao.PeerRefChannelService;
import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTask;
import cn.shijinshi.fabricmanager.dao.entity.OrgRefChannel;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.PeerRefChannel;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManageChaincode;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManageException;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManager;
import cn.shijinshi.fabricmanager.service.fabric.channel.OrgConfigModifyBean;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChannelHelper;
import cn.shijinshi.fabricmanager.service.fabric.helper.SyncData4Chain;
import cn.shijinshi.fabricmanager.service.fabric.tools.ConfigTxGen;
import cn.shijinshi.fabricmanager.service.fabric.tools.FabricToolsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChannelManageService {
    private static final Logger LOGGER = Logger.getLogger(ChannelManageService.class);

    @Autowired
    private ChannelConfigTaskService taskService;
    @Autowired
    private OrgService orgService;
    @Autowired
    private OrgRefChannelService orgRefChannelService;
    @Autowired
    private PeerRefChannelService peerRefChannelService;

    @Autowired
    private ConfigTxGen configtxgen;
    @Autowired
    private ChannelHelper channelHelper;
    @Autowired
    private MemberManager memberManager;
    @Autowired
    private MemberManageChaincode memberManageChaincode;

    //----------------------------------------------- organization -----------------------------------------------------
    public File getOrgConfig() {
        try {
            return configtxgen.createOrgConfig();
        } catch (FabricToolsException e) {
            LOGGER.error(e);
            throw new ServiceException("组织配置生成失败", e);
        }
    }

    public void updateOrgConfig(String channelName, String orgName, String description) {
        String requestId = memberManager.updateOrgConfig(channelName, orgName, description, new OrgConfigModifyBean());
        //fixme Set the delay to be greater than the block generation time, or listen to this type of request auto-commit
        submitRequest(requestId);
    }

    public void addOrganization(String channelName, String orgName, MultipartFile orgConfig, String description) {
        Map orgConfigMap;
        try {
            orgConfigMap = new ObjectMapper().readValue(orgConfig.getInputStream(), Map.class);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("用户上传的组织配置文件读取失败");
        }
        memberManager.addOrganization(channelName, orgName, orgConfigMap, description);
    }

    public void deleteOrganization(String channelName, String orgName, String description) {
        memberManager.deleteOrganization(channelName, orgName, description);
    }

    public void channelConfigTaskResponse(String requestId, boolean reject, String reason) {
        ChannelConfigTask task = getChannelConfigTask(requestId);
        if ("signing".equals(task.getStatus())) {
            String channelId = task.getChannelId();
            String requester = task.getRequester();
            byte[] updatedConfigBytes = task.getContent();
            memberManager.channelConfigTaskResponse(channelId, requestId, requester, updatedConfigBytes, reject, reason);
            taskService.updateResponse(requestId, reject, reason);
        }
    }

    /**
     * 提交一个任务到orderer节点
     */
    public void submitRequest(String requestId) {
        ChannelConfigTask task = getChannelConfigTask(requestId);
        String status = task.getStatus();
        String channelId = task.getChannelId();
        if ("signing".equals(status)) {
            byte[] channelConfigurationBytes;
            try {
                channelConfigurationBytes = channelHelper.getChannelConfigurationBytes(channelId);
            } catch (Exception e) {
                LOGGER.error(e);
                throw new ServiceException("通道"+channelId+"配置获取失败");
            }
            Configtx.Config config;
            try {
                config = Configtx.Config.parseFrom(channelConfigurationBytes);
            } catch (Exception e) {
                LOGGER.error(e);
                throw new ServiceException("通道配置数据解析失败");
            }
            long configVersion = config.getSequence();
            if (configVersion != task.getChannelConfigVersion()) {
                String msg = String.format("任务%s创建时通道配置版本为%d与当前版本%d不符，通道配置交易无法提交", requestId, task.getChannelConfigVersion(), configVersion);
                try {
                    memberManageChaincode.updateRequestState(channelId, task.getRequestId(), MemberManageChaincode.RequestState.INVALID);
                    taskService.updateStatus(task.getRequestId(), "invalid");
                }catch (MemberManageException e) {
                    LOGGER.warn(e);
                }
                throw new ServiceException(msg);
            }

            byte[] updatedConfigBytes = task.getContent();
            memberManager.submitChannelConfigToOrderer(channelId, updatedConfigBytes, requestId);
            try {
                memberManageChaincode.updateRequestState(channelId, requestId, MemberManageChaincode.RequestState.END);
                taskService.updateStatus(requestId, "end");
            }catch (MemberManageException e) {
                throw new ServiceException("通道配置更新成功, 但是任务状态更新失败.", e);
            }finally {
                if (MemberManageChaincode.RequestType.ADD_MEMBER.getType().equals(task.getRequestType())) {
                    SyncData4Chain.syncOrgRefChannel();
                } else if (MemberManageChaincode.RequestType.DELETE_MEMBER.getType().equals(task.getRequestType())) {
                    orgRefChannelService.deleteOrgRefChannelByChannel(task.getChannelId());
                    SyncData4Chain.syncOrgRefChannel();
                }
            }
        } else {
            throw new ServiceException("任务" + requestId + "当前处于" + status + "状态，无法提交");
        }
    }

    public void recallMyRequest(String requestId) {
        ChannelConfigTask task = getChannelConfigTask(requestId);
        String status = task.getStatus();
        if ("invalid".equals(status)) {
            throw new ServiceException("任务" + requestId + "当前已处于无效状态");
        } else if ("end".equals(status)) {
            throw new ServiceException("任务" + requestId + "已结束，无法撤回");
        } else if ("signing".equals(status)) {
            try {
                memberManageChaincode.updateRequestState(task.getChannelId(), task.getRequestId(), MemberManageChaincode.RequestState.INVALID);
                taskService.updateStatus(task.getRequestId(), "invalid");
            } catch (MemberManageException e) {
                throw new ServiceException(String .format("Task %s recall failed.", requestId), e);
            }
        } else {
            throw new ServiceException("任务" + requestId + "处于未知状态" + status + "请确保数据正常");
        }
    }


    public void deleteTask(String requestId) {
        ChannelConfigTask channelConfigTask = getChannelConfigTask(requestId);
        String status = channelConfigTask.getStatus();
        if ("end".equals(status) || "invalid".equals(status)) {
            taskService.deleteChannelConfigTask(requestId);
        } else {
            throw new ServiceException("任务" + requestId + "当前状态为" + status + "不可以删除");
        }
    }

    private ChannelConfigTask getChannelConfigTask(String requestId) {
        ChannelConfigTask channelConfigTask = taskService.getChannelConfigTask(requestId);
        if (channelConfigTask == null) {
            throw new ServiceException("不存在ID为" + requestId + "的任务");
        }
        return channelConfigTask;
    }

    //----------------------------------------------- channel ----------------------------------------------------------
    public void createChannel(String channelName, String consortium) {
        byte[] createChannelTx;
        try {
            createChannelTx = configtxgen.createChannelTx(channelName, consortium);
        } catch (FabricToolsException e) {
            LOGGER.error(e);
            throw new ServiceException("初始化创建通道交易失败", e);
        }

        byte[] signature;
        try {
            signature = channelHelper.getChannelConfigurationSignature(createChannelTx);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("创建通道交易签名失败", e);
        }
        try {
            channelHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, createChannelTx, signature);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道创建失败", e);
        }
    }

    public void peerJoinChannel(String channelName, String peerName) {
        try {
            channelHelper.peerJoinChannel(channelName, peerName);
            SyncData4Chain.syncPeerRefChannel();
            SyncData4Chain.syncOrgRefChannel();
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("节点" + peerName + "加入通道" + channelName + "失败", e);
        }
    }

    public List<String> getChannelMspId(String channelName) {
        List<String> mspIds = new ArrayList<>();
        List<OrgRefChannel> orgRefChannels = orgRefChannelService.selectOrgRefChannelByChannel(channelName);
        if (orgRefChannels != null && !orgRefChannels.isEmpty()) {
            for (OrgRefChannel orgRefChannel : orgRefChannels) {
                mspIds.add(orgRefChannel.getOrgMsp());
            }
        }
        return mspIds;
    }

    public List<Map> getTasks() {
        List<Map> data = new ArrayList<>();
        List<ChannelConfigTask> tasks = taskService.selectChannelConfigTask();
        if (tasks != null) {
            String mspId = orgService.getMspId();
            for (ChannelConfigTask task : tasks) {
                Map<String, Object> mate = new HashMap<>();
                mate.put("taskId", task.getRequestId());
                mate.put("requester", task.getRequester());
                mate.put("description", task.getDescription());
                mate.put("status", task.getStatus());
                mate.put("date", task.getRequestTime().getTime());
                if (mspId.equals(task.getRequester())) {
                    mate.put("owner", true);
                } else {
                    mate.put("owner", false);
                }
                data.add(mate);
            }
        }
        return data;
    }

    public Map<String, Object> getTask(String taskId) {
        Map<String, Object> data = new HashMap<>();
        ChannelConfigTask task = taskService.getChannelConfigTask(taskId);
        if (task != null) {
            data.put("channelId", task.getChannelId());
            data.put("requestId", task.getRequestId());
            data.put("requester", task.getRequester());
            data.put("requestType", task.getRequestType());
            data.put("description", task.getDescription());
            data.put("status", task.getStatus());
            try {
                Configtx.ConfigUpdate configUpdate = Configtx.ConfigUpdate.parseFrom(task.getContent());
                String content = JsonFormat.printer().print(configUpdate);
                data.put("content", content);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(e);
                data.put("content", "交易数据解析失败");
            }
            data.put("date", task.getRequestTime());
        }
        return data;
    }

    public static class ChannelInfo {
        private List<String> peers;
        private List<OrgInfo> orgs;

        public ChannelInfo() {
            peers = new ArrayList<>();
            orgs = new ArrayList<>();
        }

        public void addPeer(String peerName) {
            peers.add(peerName);
        }

        public void addOrg(OrgInfo orgInfo) {
            orgs.add(orgInfo);
        }

        public List<String> getPeers() {
            return peers;
        }

        public List<OrgInfo> getOrgs() {
            return orgs;
        }

        public static class OrgInfo {
            private String msp;
            private String anchorPeers;

            public OrgInfo(String msp, String anchorPeers) {
                this.msp = msp;
                this.anchorPeers = anchorPeers;
            }

            public String getMsp() {
                return msp;
            }

            public String getAnchorPeers() {
                return anchorPeers;
            }
        }
    }

    public Map<String, ChannelInfo> getChannelsInfo() {
        List<OrgRefChannel> orgRefChannels = orgRefChannelService.selectAllOrgRefChannel();
        List<PeerRefChannel> peerRefChannels = peerRefChannelService.selectAllPeerRefChannel();
        return formatChannelInfo(orgRefChannels, peerRefChannels);
    }

    public Map<String, ChannelInfo> getChannelInfo(String channelName) {
        List<OrgRefChannel> orgRefChannels = orgRefChannelService.selectOrgRefChannelByChannel(channelName);
        List<PeerRefChannel> peerRefChannels = peerRefChannelService.selectPeerRefChannelBuChannel(channelName);
        return formatChannelInfo(orgRefChannels, peerRefChannels);
    }

    private Map<String, ChannelInfo> formatChannelInfo(List<OrgRefChannel> orgRefChannels, List<PeerRefChannel> peerRefChannels) {
        Map<String, ChannelInfo> channelsInfo = new HashMap<>();
        if (peerRefChannels != null && !peerRefChannels.isEmpty()) {
            for (PeerRefChannel peerRefChannel : peerRefChannels) {
                String channelName = peerRefChannel.getChannelName();
                if (channelsInfo.containsKey(channelName)) {
                    ChannelInfo channelInfo = channelsInfo.get(channelName);
                    channelInfo.addPeer(peerRefChannel.getPeerName());
                } else {
                    ChannelInfo channelInfo = new ChannelInfo();
                    channelInfo.addPeer(peerRefChannel.getPeerName());
                    channelsInfo.put(channelName, channelInfo);
                }
            }
        }

        if (orgRefChannels != null && !orgRefChannels.isEmpty()) {
            for (OrgRefChannel orgRefChannel : orgRefChannels) {
                ChannelInfo channelInfo = channelsInfo.get(orgRefChannel.getChannelName());
                if (channelInfo != null) {
                    String anchorPeers = orgRefChannel.getAnchorPeers();
                    ChannelInfo.OrgInfo orgInfo = new ChannelInfo.OrgInfo(orgRefChannel.getOrgMsp(), anchorPeers);
                    channelInfo.addOrg(orgInfo);
                }
            }
        }
        return channelsInfo;
    }

}
