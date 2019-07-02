package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.OrdererNodeService;
import cn.shijinshi.fabricmanager.dao.entity.OrdererNode;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChannelHelper;
import cn.shijinshi.fabricmanager.service.fabric.tools.ConfigTxLator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ConsortiumManageService {
    private static final Logger LOGGER = Logger.getLogger(ConsortiumManageService.class);
    private final ChannelHelper channelHelper;
    private final OrdererNodeService ordererNodeService;
    private final ConfigTxLator configtxlator;

    @Autowired
    public ConsortiumManageService(ChannelHelper channelHelper, OrdererNodeService ordererNodeService, ConfigTxLator configtxlator) {
        this.channelHelper = channelHelper;
        this.ordererNodeService = ordererNodeService;
        this.configtxlator = configtxlator;
    }

    public Map<String, Set<String>> getConsortiums(String ordererName) {
        Map<String, Set<String>> consortiumsMap = new HashMap<>();
        OrdererNode orderer = getOrderer(ordererName);
        byte[] systemChainConfigBytes = getSystemChainConfigBytes(orderer.getSystemChain(), ordererName);
        Configtx.Config systemChainConfig;
        try {
            systemChainConfig = Configtx.Config.parseFrom(systemChainConfigBytes);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置数据解析失败");
        }
        Configtx.ConfigGroup consortiums = systemChainConfig.getChannelGroup().getGroupsMap().get("Consortiums");
        if (consortiums != null) {
            Map<String, Configtx.ConfigGroup> consortiumsGroupsMap = consortiums.getGroupsMap();
            for (Map.Entry<String, Configtx.ConfigGroup> groupEntry : consortiumsGroupsMap.entrySet()) {
                String consortiumName = groupEntry.getKey();
                Configtx.ConfigGroup consortium = groupEntry.getValue();
                Set<String> membersName = consortium.getGroupsMap().keySet();
                consortiumsMap.put(consortiumName, membersName);
            }
        }
        return consortiumsMap;
    }

    @SuppressWarnings("unchecked")
    public void orgJoinConsortium(String ordererName, String consortiumName, String orgName, MultipartFile orgConfig) {
        //读取用户上传的组织配置文件
        Map orgConfigMap;
        try {
            orgConfigMap = new ObjectMapper().readValue(orgConfig.getInputStream(), Map.class);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("用户上传的组织配置文件读取失败");
        }
        //获取系统通道配置
        OrdererNode orderer = getOrderer(ordererName);
        byte[] systemChainConfigBytes = getSystemChainConfigBytes(orderer.getSystemChain(), ordererName);
        //解码通道配置
        Map configMap = decodeChainConfig(systemChainConfigBytes);
        //修改通道配置
        Map consortiums = getConsortiums(configMap);
        if (consortiums == null) {
            throw new ServiceException("系统通道配置读取失败，配置格式不正确");
        }
        if (consortiums.containsKey(consortiumName)) {
            Map consortium = (Map) consortiums.get(consortiumName);
            if (consortium == null) {
                throw new ServiceException("获取到的联盟" + consortiumName + "配置信息为空，无法添加组织到联盟");
            }
            if (consortium.containsKey("groups")) {
                Map groups = (Map) consortium.get("groups");
                if (groups == null) {
                    groups = new HashMap();
                } else if (groups.containsKey(orgName)) {
                    throw new ServiceException("联盟" + consortiumName + "中已存在名为" + orgName + "的成员");
                }
                groups.put(orgName, orgConfigMap);
            } else {
                Map groups = new HashMap<>();
                groups.put(orgName, orgConfigMap);
                consortium.put("groups", groups);
            }
        } else {
            throw new ServiceException("Orderer节点" + ordererName + "上的系统通道不存在名为" + consortiumName + "的联盟");
        }
        //计算增量，然后签名提交给orderer
        submitNewConfig(systemChainConfigBytes, configMap, ordererName, orderer.getSystemChain());
    }

    public void deleteOrgFromConsortium(String ordererName, String consortiumName, String orgName) {
        //获取系统通道配置
        OrdererNode orderer = getOrderer(ordererName);
        byte[] systemChainConfigBytes = getSystemChainConfigBytes(orderer.getSystemChain(), ordererName);
        //解码通道配置
        Map configMap = decodeChainConfig(systemChainConfigBytes);
        //修改通道配置
        Map consortiums = getConsortiums(configMap);
        if (consortiums == null) {
            throw new ServiceException("系统通道配置读取失败，配置格式不正确");
        }
        if (consortiums.containsKey(consortiumName)) {
            Map consortium = (Map) consortiums.get(consortiumName);
            if (consortium == null) {
                throw new ServiceException("获取到的联盟" + consortiumName + "配置信息为空，无法完成删除组织");
            }
            if (consortium.containsKey("groups")) {
                Map groups = (Map) consortium.get("groups");
                if (groups != null && groups.containsKey(orgName)) {
                    groups.remove(orgName);
                } else {
                    throw new ServiceException("联盟" + consortiumName + "中不存在名为" + orgName + "的组织");
                }
            }
        } else {
            throw new ServiceException("Orderer节点" + ordererName + "上的系统通道不存在名为" + consortiumName + "的联盟");
        }
        //计算增量，然后签名提交给orderer
        submitNewConfig(systemChainConfigBytes, configMap, ordererName, orderer.getSystemChain());
    }

    private OrdererNode getOrderer(String ordererName) {
        Organization organization = Context.getOrganization();
        if (!Organization.ORG_TYPE_ORDERER.equals(organization.getOrgType())) {
            throw new ServiceException("只有Orderer组织才能管理联盟成员");
        }
        OrdererNode ordererNode = ordererNodeService.getOrderer(ordererName);
        if (ordererNode == null) {
            throw new ServiceException("不存在Orderer节点" + ordererName);
        }
        return ordererNode;
    }

    private byte[] getSystemChainConfigBytes(String systemChainId, String ordererName) {
        try {
            return channelHelper.getChannelConfigurationBytesFromOrderer(systemChainId, ordererName);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("系统通道配置获取失败", e);
        }
    }

    private Map getConsortiums(Map configMap) {
        try {
            if (configMap != null && configMap.containsKey("channel_group")) {
                Map channel_group = (Map) configMap.get("channel_group");
                if (channel_group != null && channel_group.containsKey("groups")) {
                    Map groups = (Map) channel_group.get("groups");
                    if (groups != null && groups.containsKey("Consortiums")) {
                        Map consortiums = (Map) groups.get("Consortiums");
                        if (consortiums != null && consortiums.containsKey("groups")) {
                            return (Map) consortiums.get("groups");
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new ServiceException("系统通道配置读取失败，配置格式不正确");
        }
        return null;
    }

    private Map decodeChainConfig(byte[] systemChainConfigBytes) {
        try {
            String decode = configtxlator.decode(systemChainConfigBytes, ConfigTxLator.ProtoType.CONFIG);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decode, Map.class);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("系统通道配置解析失败");
        }
    }

    private void submitNewConfig(byte[] original, Map modifiedMap, String ordererName, String systemChainId) {
        //编码修改后的配置
        byte[] modifiedConfigBytes;
        try {
            String configStr = new ObjectMapper().writeValueAsString(modifiedMap);
            modifiedConfigBytes = configtxlator.encode(configStr, ConfigTxLator.ProtoType.CONFIG);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("更新后的系统通道配置编码失败", e);
        }
        //计算配置增量
        byte[] updatedConfigBytes;
        try {
            updatedConfigBytes = configtxlator.computeUpdate(original, modifiedConfigBytes, systemChainId);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置更新增量计算失败");
        }
        //签名配置交易
        byte[] signature;
        try {
            signature = channelHelper.getUpdateChannelConfigurationSignature(updatedConfigBytes);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置交易签名失败", e);
        }
        //提交给orderer节点
        try {
            channelHelper.submitChannelConfig(systemChainId, ordererName, updatedConfigBytes, signature);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServiceException("通道配置更新交易提交失败", e);
        }
    }
}
