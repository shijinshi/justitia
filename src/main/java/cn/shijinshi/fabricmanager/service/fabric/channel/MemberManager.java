package cn.shijinshi.fabricmanager.service.fabric.channel;

import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChannelHelper;
import cn.shijinshi.fabricmanager.service.fabric.tools.ConfigTxLator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.hyperledger.fabric.protos.msp.MspConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemberManager {
    private static final Logger LOGGER = Logger.getLogger(MemberManager.class);

    private final ChannelHelper channelHelper;
    private final ConfigTxLator configtxlator;
    private final MemberManageChaincode memberManageChaincode;


    @Autowired
    public MemberManager(ChannelHelper channelHelper, ConfigTxLator configtxlator, MemberManageChaincode memberManageChaincode) {
        this.channelHelper = channelHelper;
        this.configtxlator = configtxlator;
        this.memberManageChaincode = memberManageChaincode;
    }

    public String updateOrgConfig(String channelName, String orgName, String description, OrgConfigModifyBean data) {
        return createSignRequest(channelName, orgName, description, data, MemberManageChaincode.RequestType.MODIFY_ORG_CONFIG);
    }

    public String addOrganization(String channelName, String orgName, Map orgConfig, String description) {
        return createSignRequest(channelName, orgName, description, orgConfig, MemberManageChaincode.RequestType.ADD_MEMBER);
    }

    public String deleteOrganization(String channelName, String orgName, String description) {
       return createSignRequest(channelName, orgName, description, null, MemberManageChaincode.RequestType.DELETE_MEMBER);
    }

    private String createSignRequest(String channelName, String orgName, String description, Object data, MemberManageChaincode.RequestType type) {
        byte[] channelConfigBytes = getChannelConfigBytes(channelName);
        Set<String> orgsMsp = getOrgsMsp(channelConfigBytes);
        Map config = decodeChainConfig(channelConfigBytes);
        if (!config.containsKey("sequence")) {
            throw new ServiceException("通道配置读取失败，配置格式不正确");
        }
        String configVersion = (String) config.get("sequence");
        Map groups = getApplicationGroups(config);
        if (groups == null) {
            throw new ServiceException("通道配置读取失败，配置格式不正确");
        }
        switch (type) {
            case ADD_MEMBER:
                if (groups.containsKey(orgName)) {
                    throw new ServiceException("通道" + channelName + "中已存在名为" + orgName + "的组织");
                } else {
                    if (data instanceof Map) {
                        groups.put(orgName, data);
                    } else {
                        throw  new ServiceException("组织配置数据不是一个json格式");
                    }
                }
                break;
            case DELETE_MEMBER:
                if (groups.containsKey(orgName)) {
                    groups.remove(orgName);
                } else {
                    throw new ServiceException("通道" + channelName + "中不包含名为" + orgName + "的组织");
                }
                break;
            case MODIFY_ORG_CONFIG:
                if (groups.containsKey(orgName)) {
                    if (data instanceof OrgConfigModifyBean) {
                        modifyOrgConfig((Map)groups.get(orgName));
                    }
                } else {
                    throw new ServiceException("通道" + channelName + "中不包含名为" + orgName + "的组织");
                }
                break;
            default:
                throw new ServiceException("未知的请求类型：" + type);
        }
        byte[] updatedConfig = createTransaction(channelConfigBytes, config, channelName);
        try {
            return memberManageChaincode.signRequests(channelName, updatedConfig, type, configVersion, description, orgsMsp);
        } catch (MemberManageException e) {
            LOGGER.error(e);
            throw new ServiceException("成员管理请求（待签名）发起失败", e);
        }
    }

    private byte[] getChannelConfigBytes(String channelName) {
        try {
            return channelHelper.getChannelConfigurationBytes(channelName);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("系统通道配置获取失败", e);
        }
    }

    private Set<String> getOrgsMsp(byte[] channelConfigBytes) {
        Set<String> orgsMsp = new HashSet<>();
        try {
            Configtx.Config channelConfig = Configtx.Config.parseFrom(channelConfigBytes);
            Map<String, Configtx.ConfigGroup> groupsMap = channelConfig.getChannelGroup().getGroupsMap().get("Application").getGroupsMap();
            for (Configtx.ConfigGroup group : groupsMap.values()) {
                ByteString value = group.getValuesMap().get("MSP").getValue();
                ByteString config = MspConfig.MSPConfig.parseFrom(value).getConfig();
                String msp = MspConfig.FabricMSPConfig.parseFrom(config).getName();
                orgsMsp.add(msp);
            }
        }catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("道配置读取失败，配置格式不正确");
        }
        return orgsMsp;
    }

    private Map decodeChainConfig(byte[] systemChainConfigBytes) {
        try {
            String decode = configtxlator.decode(systemChainConfigBytes, ConfigTxLator.ProtoType.CONFIG);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decode, Map.class);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置解析失败", e);
        }
    }

    private Map getApplicationGroups(Map channelConfig) {
        if (channelConfig == null) {
            throw new ServiceException("通道配置为空");
        }

        try {
            if (channelConfig.containsKey("channel_group")) {
                Map channel_group = (Map) channelConfig.get("channel_group");
                if (channel_group != null && channel_group.containsKey("groups")) {
                    Map groups = (Map) channel_group.get("groups");
                    if (groups != null && groups.containsKey("Application")) {
                        Map application = (Map) groups.get("Application");
                        if (application != null && application.containsKey("groups")) {
                            return (Map) application.get("groups");
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new ServiceException("系统通道配置读取失败，配置格式不正确");
        }
        return null;
    }

    private void modifyOrgConfig(Map orgConfig) {

    }

    private byte[] createTransaction(byte[] original, Map modifiedMap, String channelId) {
        //编码修改后的配置
        byte[] modifiedConfigBytes;
        try {
            String configStr = new ObjectMapper().writeValueAsString(modifiedMap);
            modifiedConfigBytes = configtxlator.encode(configStr, ConfigTxLator.ProtoType.CONFIG);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("更新后的通道配置编码失败", e);
        }
        //计算配置增量
        try {
            return configtxlator.computeUpdate(original, modifiedConfigBytes, channelId);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置更新增量计算失败");
        }
    }

    public void channelConfigTaskResponse(String channelId, String requestId, String requester, byte[] updatedConfigBytes, boolean reject, String reason) {
        byte[] signature;
        try {
            signature = channelHelper.getUpdateChannelConfigurationSignature(updatedConfigBytes);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("更新通道配置交易签名失败");
        }
        try {
            memberManageChaincode.signResponses(channelId, requestId, requester, reject, signature, reason);
        } catch (MemberManageException e) {
            LOGGER.error(e);
            throw new ServiceException("任务响应失败.", e);
        }
    }

    public void submitChannelConfigToOrderer(String channelId, byte[] updatedConfigBytes, String requestId) {
        ArrayList<byte[]> signatures = new ArrayList<>();
        List<MemberManageChaincode.SignResponse> signResponses;
        try {
            signResponses = memberManageChaincode.getAllSignResponses(channelId, requestId);
        } catch (MemberManageException e) {
            LOGGER.error(e);
            throw new ServiceException("获取其他通道成员的签名数据失败", e);
        }
        for (MemberManageChaincode.SignResponse response :signResponses) {
            signatures.add(Base64.getDecoder().decode(response.getSignature()));
        }

        //签名配置交易
        try {
            byte[] selfSignature = channelHelper.getUpdateChannelConfigurationSignature(updatedConfigBytes);
            signatures.add(selfSignature);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置交易签名失败", e);
        }
        //提交交易到orderer节点
        try {
            channelHelper.submitChannelConfig(channelId, Organization.DEFAULT_ORDERER_NAME, updatedConfigBytes, signatures.toArray(new byte[signatures.size()][]));
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置更新交易提交失败", e);
        }
    }

}
