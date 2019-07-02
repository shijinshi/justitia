package cn.shijinshi.fabricmanager.service.fabric.helper;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ContextException;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.common.Configtx;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ChannelHelper {
    @Autowired
    private HFClientHelper clientHelper;


    public Set<String> queryChannels(String peerName) throws InvalidArgumentException, ProposalException, FabricClientException {
        HFClient client = clientHelper.getHFClient();
        Peer peer = clientHelper.createPeer(peerName);
        return client.queryChannels(peer);
    }

    public BlockchainInfo getBlockChainInfo(String channelName) throws InvalidArgumentException, ProposalException, FabricClientException, TransactionException {
        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        return channel.queryBlockchainInfo();
    }

    public byte[] getChannelConfigurationBytes(String channelName) throws TransactionException, ContextException, InvalidArgumentException, FabricClientException {
        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        return channel.getChannelConfigurationBytes();
    }

    public long getConfigBlockNumber(String channelName) throws TransactionException, FabricClientException, InvalidArgumentException,
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        Class<?> channelClass = Class.forName("org.hyperledger.fabric.sdk.Channel");
        Method method = channelClass.getDeclaredMethod("getConfigBlock", List.class);
        method.setAccessible(true);
        ArrayList<Peer> peers = new ArrayList<>(channel.getPeers());
        Collections.shuffle(peers);
        Common.Block configBlock = (Common.Block) method.invoke(channel, peers);
        return configBlock.getHeader().getNumber();
    }

    public byte[] getChannelConfigurationBytesFromOrderer(String channelName, String ordererName) throws TransactionException,
            FabricClientException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InvalidProtocolBufferException {

        Channel channel = clientHelper.createChannelNonPeer(channelName, ordererName);
        Class<?> channelClass = Class.forName("org.hyperledger.fabric.sdk.Channel");
        Method method = channelClass.getDeclaredMethod("getConfigurationBlock");
        method.setAccessible(true);
        Common.Block configBlock = (Common.Block) method.invoke(channel);
        Common.Envelope envelopeRet = Common.Envelope.parseFrom(configBlock.getData().getData(0));
        Common.Payload payload = Common.Payload.parseFrom(envelopeRet.getPayload());
        Configtx.ConfigEnvelope configEnvelope = Configtx.ConfigEnvelope.parseFrom(payload.getData());
        return configEnvelope.getConfig().toByteArray();
    }

    public byte[] getChannelConfigurationSignature(byte[] channelConfigBytes) throws InvalidArgumentException, FabricClientException {
        ChannelConfiguration channelConfig = new ChannelConfiguration(channelConfigBytes);
        HFClient client = clientHelper.getHFClient();
        User user = client.getUserContext();
        return client.getChannelConfigurationSignature(channelConfig, user);
    }
    public byte[] getUpdateChannelConfigurationSignature(byte[] configBytes) throws InvalidArgumentException, FabricClientException {
        UpdateChannelConfiguration updateChannelConfig = new UpdateChannelConfiguration(configBytes);
        HFClient client = clientHelper.getHFClient();
        User user = client.getUserContext();
        return client.getUpdateChannelConfigurationSignature(updateChannelConfig, user);
    }

    public void submitChannelConfig(String channelName, String ordererName, byte[] configBytes, byte[]... signers)
            throws TransactionException, FabricClientException, InvalidArgumentException {
        UpdateChannelConfiguration updateChannelConfig = new UpdateChannelConfiguration(configBytes);
        Channel channel = clientHelper.createChannelNonPeer(channelName, ordererName);
        channel.updateChannelConfiguration(updateChannelConfig, signers);
    }

    public void createChannel(String channelName, String ordererName, byte[] channelConfigBytes, byte[]... signers) throws FabricClientException,
            InvalidArgumentException, TransactionException {

        ChannelConfiguration channelConfig = new ChannelConfiguration(channelConfigBytes);
        Orderer orderer = clientHelper.createOrderer(ordererName);
        if (orderer == null) {
            throw new InvalidArgumentException("Not found orderer by name " + ordererName);
        }
        HFClient client = clientHelper.getHFClient();
        client.newChannel(channelName, orderer, channelConfig, signers);
    }

    public void peerJoinChannel(String channelName, String peerName) throws FabricClientException, InvalidArgumentException, ProposalException {
        Orderer orderer = clientHelper.createOrderer(Organization.DEFAULT_ORDERER_NAME);
        if (orderer == null) {
            throw new InvalidArgumentException("Not found orderer by name " + Organization.DEFAULT_ORDERER_NAME);
        }
        Peer peer = clientHelper.createPeer(peerName);
        if (peer == null) {
            throw new InvalidArgumentException("Not found peer by name " + peerName);
        }
        HFClient client = clientHelper.getHFClient();
        Channel channel = client.newChannel(channelName);
        channel.addOrderer(orderer);
        channel.joinPeer(peer);
    }
}
