package cn.shijinshi.fabricmanager;

import cn.shijinshi.fabricmanager.dao.OrdererNodeService;
import cn.shijinshi.fabricmanager.dao.OrganizationService;
import cn.shijinshi.fabricmanager.dao.PeerNodeService;
import cn.shijinshi.fabricmanager.dao.PeerRefChannelService;
import cn.shijinshi.fabricmanager.dao.entity.OrdererInfo;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.dao.entity.PeerInfo;
import cn.shijinshi.fabricmanager.exception.ContextException;
import cn.shijinshi.fabricmanager.service.fabric.helper.SyncData4Chain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("Context")
public class Context implements InitializingBean {
    private static final Logger LOGGER = Logger.getLogger(Context.class);

    private final static Map<String, PeerInfo> peerInfoMap = new ConcurrentHashMap<>();
    private final static Map<String, OrdererInfo> ordererInfoMap = new ConcurrentHashMap<>();
    private static Organization organization = null;

    private static OrganizationService organizationService;
    private static OrdererNodeService ordererNodeService;
    private static PeerNodeService peerNodeService;
    private static SyncData4Chain syncData4Chain;

    private static PeerRefChannelService peerRefChannelService;


    @Autowired
    private Context(OrganizationService orgService,  OrdererNodeService ordererNodeService,PeerNodeService peerNodeService,
                    SyncData4Chain syncData4Chain, PeerRefChannelService peerRefChannelService) {
        Context.organizationService = orgService;
        Context.ordererNodeService = ordererNodeService;
        Context.peerNodeService = peerNodeService;
        Context.syncData4Chain = syncData4Chain;
        Context.peerRefChannelService = peerRefChannelService;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            resetContext();
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }

    public static boolean checkContext(boolean checkOrg, boolean checkPeer, boolean checkOrderer) {
        if (checkOrg && organization == null) {
            return false;
        }
        if (checkPeer && peerInfoMap.isEmpty()) {
            return false;
        }
        if (checkOrderer && ordererInfoMap.isEmpty()) {
            return false;
        }
        return true;
    }

    public synchronized static void resetContext() {
        Context.organization = organizationService.getOrg();
        if (Context.organization != null) {
            loadOrdererInfo();
            if (Organization.ORG_TYPE_PEER.equals(organization.getOrgType())) {
                loadPeerInfo();
                syncData4Chain.begin();
            }
        }
    }

    public synchronized static void setTlsServer(String serverName) {
        Organization organization = getOrganization();
        if (StringUtils.isEmpty(organization.getTlsCaServer())) {
            organization.setTlsCaServer(serverName);
            organizationService.updateTlsCaServer(organization);
        }
    }

    public synchronized static void delTlsServer(String serverName) {
        Organization organization = getOrganization();
        if (StringUtils.isEmpty(serverName) && serverName.equals(organization.getTlsCaServer())) {
            organization.setTlsCaServer(null);
            organizationService.updateTlsCaServer(organization);
        }
    }


    public static Organization getOrganization() {
        if (organization == null) {
            resetContext();
            if (organization == null) {
                throw new ContextException("没有获取到组织配置信息，请先配置系统组织");
            }
        }
        return organization;
    }

    public static void loadPeerInfo() {
        synchronized (peerInfoMap) {        //加载节点数据的时候避免其他线程调用get方法
            peerInfoMap.clear();
            List<PeerInfo> peersInfo = peerNodeService.selectAllPeerInfo();
            if (peersInfo != null && !peersInfo.isEmpty()) {
                for (PeerInfo peerInfo : peersInfo) {
                    String ip = peerInfo.getIp();
                    String tlsCert = peerInfo.getTlsCert();
                    if (StringUtils.isEmpty(ip) || (peerInfo.isTlsEnable() && StringUtils.isEmpty(tlsCert))) {
                        continue;
                    }
                    int serverPort;
                    try {
                        serverPort = getServerPortExposed(peerInfo.getExposedPort(), peerInfo.getServerPort());
                    } catch (IOException e) {
                        continue;
                    }
                    if (serverPort == 0) {
                        continue;
                    }

                    //url
                    StringBuilder url = new StringBuilder();
                    String protocol = peerInfo.isTlsEnable() ? "grpcs://" : "grpc://";
                    url.append(protocol).append(ip).append(":").append(serverPort);
                    peerInfo.setUrl(url.toString());

                    //properties
                    Properties properties = initProperties(peerInfo.isTlsEnable(), tlsCert, null, null);
                    peerInfo.setProperties(properties);

                    peerInfoMap.put(peerInfo.getPeerName(), peerInfo);
                }
            }
        }
    }

    public static void loadOrdererInfo() {
        synchronized (ordererInfoMap) {     //加载节点数据的时候避免其他线程调用get方法
            ordererInfoMap.clear();
            Organization organization = getOrganization();
            if (Organization.ORG_TYPE_PEER.equals(organization.getOrgType())) {
                String ordererName = Organization.DEFAULT_ORDERER_NAME;
                String ip = organization.getOrdererIp();
                Integer port = organization.getOrdererPort();
                String ordererTlsCert = organization.getOrdererTlsCert();
                if (StringUtils.isEmpty(ip) || port == null || port == 0) {
                    throw new ContextException("组织类型为peer组织，但是orderer节点请求地址丢失");
                }

                StringBuilder url = new StringBuilder();
                Properties properties = null;
                if (StringUtils.isNotEmpty(ordererTlsCert)) {
                    url.append("grpcs://");
                    properties = initProperties(true, ordererTlsCert, null, null);
                } else {
                    url.append("grpc://");
                }
                url.append(ip).append(":").append(port);
                OrdererInfo ordererInfo = new OrdererInfo();
                ordererInfo.setOrdererName(ordererName);
                ordererInfo.setUrl(url.toString());
                ordererInfo.setProperties(properties);
                ordererInfoMap.put(ordererName, ordererInfo);
            } else if (Organization.ORG_TYPE_ORDERER.equals(organization.getOrgType())) {
                List<OrdererInfo> orderersInfo = ordererNodeService.selectOrdererInfo();
                if (orderersInfo != null && !orderersInfo.isEmpty()) {
                    for (OrdererInfo ordererInfo : orderersInfo) {
                        String ip = ordererInfo.getIp();
                        String tlsCert = ordererInfo.getTlsCert();
                        if (StringUtils.isEmpty(ip) || (ordererInfo.isTlsEnable() && StringUtils.isEmpty(tlsCert))) {
                            continue;
                        }
                        int serverPort;
                        try {
                            serverPort = getServerPortExposed(ordererInfo.getExposedPort(), ordererInfo.getServerPort());
                        } catch (IOException e) {
                            continue;
                        }
                        if (serverPort == 0) {
                            continue;
                        }


                        //url
                        StringBuilder url = new StringBuilder();
                        String protocol = ordererInfo.isTlsEnable() ? "grpcs://" : "grpc://";
                        url.append(protocol).append(ip).append(":").append(serverPort);
                        ordererInfo.setUrl(url.toString());

                        //properties
                        Properties properties = initProperties(ordererInfo.isTlsEnable(), tlsCert, null, null);
                        ordererInfo.setProperties(properties);

                        ordererInfoMap.put(ordererInfo.getOrdererName(), ordererInfo);
                    }
                }
            } else {
                throw new ContextException("未知的组织类型:" + organization.getOrgType());
            }
        }
    }

    private synchronized static int getServerPortExposed(String exposedPort, int serverPort) throws IOException {
        if (serverPort == 0 || StringUtils.isEmpty(exposedPort)) {
            return 0;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map exposedPortMap = objectMapper.readValue(exposedPort, Map.class);
        if (exposedPortMap.isEmpty() || !exposedPortMap.containsKey(serverPort + "")) {
            return 0;
        } else {
            return (int) exposedPortMap.get(serverPort + "");
        }
    }

    private static Properties initProperties(boolean tlsEnable, String serverCert, String clientCert, String clientKey) {
        Properties properties = new Properties();
        if (tlsEnable && StringUtils.isNotEmpty(serverCert)) {
            properties.put("pemBytes", serverCert.getBytes());
            if (StringUtils.isNotEmpty(clientCert) && StringUtils.isNotEmpty(clientKey)) {
                //如果peer节点没有开启TLS双端认证这个配置就不能给，不然TLS握手失败
                properties.put("clientCertBytes", clientCert.getBytes());
                properties.put("clientKeyBytes", clientKey.getBytes());
            }

            properties.setProperty("sslProvider", "openSSL");
            properties.setProperty("negotiationType", "TLS");

            //信任服务端证书
            properties.setProperty("trustServerCertificate", "true");
        }
        return properties;
    }

    public static Set<String> getPeersName() {
        synchronized (peerInfoMap) {
            return peerInfoMap.keySet();
        }
    }

    /**
     * 获取对应通道下活跃状态的peer节点
     * @param channelName 通道名称
     * @return peer节点名称
     */
    public static Set<String> getPeersName(String channelName) {
        return peerRefChannelService.selectPeersNameByChannel(channelName);
    }

    public static PeerInfo getPeerInfo(String peerName) {
        synchronized (peerInfoMap) {
            return peerInfoMap.getOrDefault(peerName, null);
        }
    }

    public static Collection<PeerInfo> getPeersInfo() {
        synchronized (peerInfoMap) {
            return peerInfoMap.values();
        }
    }

    public static Set<String> getOrderersName() {
        synchronized (ordererInfoMap) {
            return ordererInfoMap.keySet();
        }
    }

    public static OrdererInfo getOrdererInfo(String ordererName) {
        synchronized (ordererInfoMap) {
            return ordererInfoMap.getOrDefault(ordererName, null);
        }
    }
}
