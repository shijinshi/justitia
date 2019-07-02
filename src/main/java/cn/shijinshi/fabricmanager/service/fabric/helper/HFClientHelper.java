package cn.shijinshi.fabricmanager.service.fabric.helper;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.entity.*;
import cn.shijinshi.fabricmanager.exception.ContextException;
import cn.shijinshi.fabricmanager.service.fabric.FabricUserImpl;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class HFClientHelper {
    private static final Logger LOGGER = Logger.getLogger(HFClientHelper.class);
    private static HFClient hfClient = null;
    private final FabricCaUserService fabricCaUserService;

    @Autowired
    private HFClientHelper(FabricCaUserService fabricCaUserService) {
        this.fabricCaUserService = fabricCaUserService;
    }

    public HFClient createHFClient() throws FabricClientException {
        Organization organization;
        try {
            organization = Context.getOrganization();
        } catch (ContextException e) {
            throw new FabricClientException(e.getMessage());
        }
        //获取组织管理员用户
        List<UserAndCerts> fabricCaUsers = fabricCaUserService.selectOrgAdminUser();
        if (fabricCaUsers == null || fabricCaUsers.isEmpty()) {
            throw new FabricClientException("There is no valid organization administrator user.");
        }
        UserAndCerts adminUser = null;
        for (UserAndCerts userAndCerts : fabricCaUsers) {
            Certificates certificate = userAndCerts.getCertificate();
            if (certificate != null && Certificates.STATE_GOOD.equals(certificate.getState())) {
                if (StringUtils.isEmpty(userAndCerts.getUserId()) || null == userAndCerts.getAffiliation()
                        || StringUtils.isEmpty(certificate.getCertPem()) || StringUtils.isEmpty(certificate.getKeyPem())) {
                    continue;
                }
                Date notAfter = certificate.getNotAfter();
                if (notAfter == null || notAfter.getTime() > System.currentTimeMillis()) {
                    adminUser = userAndCerts;
                }
            }
        }
        if (adminUser == null) {
            throw new FabricClientException("There is no valid organization administrator user");
        }

        FabricUserImpl fabricUser = new FabricUserImpl(adminUser.getUserId(), adminUser.getAffiliation());
        fabricUser.setMspId(organization.getOrgMspId());
        try {
            Certificates certificate = adminUser.getCertificate();
            fabricUser.setEnrollment(certificate.getCertPem(), certificate.getKeyPem());
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricClientException("User(" + adminUser.getServerName() + ":" + adminUser.getUserId() + ") certificate read failed.\n" + e.getMessage());
        }

        HFClient hfClient = HFClient.createNewInstance();
        try {
            hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            hfClient.setUserContext(fabricUser);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new FabricClientException("Fabric client identity configuration failed. \n" + e.getMessage());
        }
        return hfClient;
    }

    /**
     * 创建Fabric channel对象
     *
     * @param client      Fabric HFClient对象
     * @param channelName 通道名称
     * @param ordererName orderer节点名称
     * @param peersName   peer节点名称
     * @return Channel
     * @throws InvalidArgumentException 参数异常
     * @throws TransactionException channel对象实例化失败
     */
    public Channel createChannel(HFClient client, String channelName, String ordererName, Set<String> peersName,
                                 Channel.PeerOptions options) throws InvalidArgumentException, TransactionException {

        if (StringUtils.isEmpty(channelName)) {
            throw new InvalidArgumentException("Channel name is empty");
        }
        if (client == null) {
            throw new InvalidArgumentException("Client name is null");
        }

        Channel channel = client.getChannel(channelName);
        if (channel == null || channel.isShutdown()) {
            channel = client.newChannel(channelName);
        }

        if (StringUtils.isNotEmpty(ordererName)) {
            OrdererInfo ordererInfo = Context.getOrdererInfo(ordererName);
            if (ordererInfo == null) {
                throw new InvalidArgumentException("There is no orderer node named " + ordererName);
            }
            Collection<Orderer> orderers = channel.getOrderers();
            boolean ordererExists = false;
            for (Orderer orderer : orderers) {
                if (orderer.getUrl().equals(ordererInfo.getUrl())) {
                    ordererExists = true;
                    break;
                }
            }
            if (!ordererExists) {
                Orderer orderer = client.newOrderer(ordererInfo.getOrdererName(), ordererInfo.getUrl(), ordererInfo.getProperties());
                channel.addOrderer(orderer);
            }
        }

        if (peersName != null && !peersName.isEmpty()) {
            Collection<Peer> peers = channel.getPeers();
            for (String peerName : peersName) {
                PeerInfo peerInfo = Context.getPeerInfo(peerName);
                if (peerInfo == null) {
                    LOGGER.warn("There is no peer node named " + peerName);
                    continue;
                }
                boolean peerExists = false;
                for (Peer peer : peers) {
                    if (peer.getUrl().equals(peerInfo.getUrl())) {
                        peerExists = true;
                        break;
                    }
                }
                if (!peerExists) {
                    try {
                        Peer peer = client.newPeer(peerInfo.getPeerName(), peerInfo.getUrl(), peerInfo.getProperties());
                        if (options == null) {
                            channel.addPeer(peer);
                        } else {
                            channel.addPeer(peer, options);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Peer node " + peerName + "create failed.", e);
                        continue;
                    }
                }
            }
        }

        channel.initialize();
        return channel;
    }

    HFClient getHFClient() throws FabricClientException {
        if (hfClient == null) {
            return createHFClient();
        }
        return hfClient;
    }

    /**
     * Orderer组织创建通道
     */
    Channel createChannelNonPeer(String channelName, String ordererName) throws TransactionException, FabricClientException, InvalidArgumentException {
        return createChannel(getHFClient(), channelName, ordererName, null, null);
    }

    /**
     * Peer组织创建通道
     */
    Channel createChannel(String channelName, String ordererName, Set<String> peersName) throws InvalidArgumentException, FabricClientException, TransactionException {
        return createChannel(getHFClient(), channelName, ordererName, peersName, null);
    }

    public Peer createPeer(String peerName) throws InvalidArgumentException, FabricClientException {
        PeerInfo peerInfo = Context.getPeerInfo(peerName);
        if (peerInfo != null) {
            return getHFClient().newPeer(peerInfo.getPeerName(), peerInfo.getUrl(), peerInfo.getProperties());
        }
        return null;
    }

    public Orderer createOrderer(String ordererName) throws InvalidArgumentException, FabricClientException {
        OrdererInfo ordererInfo = Context.getOrdererInfo(ordererName);
        if (ordererInfo != null) {
            return getHFClient().newOrderer(ordererInfo.getOrdererName(), ordererInfo.getUrl(), ordererInfo.getProperties());
        }
        return null;
    }
}
