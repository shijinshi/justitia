package cn.shijinshi.fabricmanager.service.fabric.helper;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ContextException;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class ChaincodeHelper {
    @Autowired
    private HFClientHelper clientHelper;


    public List<Query.ChaincodeInfo> queryInstantiatedChaincodes(String channelName, String peerName) throws ProposalException,
            InvalidArgumentException, FabricClientException, TransactionException {

        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        Collection<Peer> peers = channel.getPeers();
        Peer peer = null;
        for (Peer peer1 : peers) {
            if (peerName.equals(peer1.getName())) {
                peer = peer1;
            }
        }
        return channel.queryInstantiatedChaincodes(peer);
    }

    public Collection<ProposalResponse> invokeChaincode(String channelName, String chaincodeName,String function, ArrayList<String> args)
            throws InvalidArgumentException, ProposalException, ContextException, FabricClientException, TransactionException {

        HFClient client = clientHelper.getHFClient();
        TransactionProposalRequest request = client.newTransactionProposalRequest();
        ChaincodeID chaincodeID = ChaincodeID.newBuilder()
                .setName(chaincodeName)
                .build();
        request.setChaincodeID(chaincodeID);
//        request.setProposalWaitTime(Config.getConfig().getProposalWaitTime());  //默认35000，也可以自定义
        request.setFcn(function);
        request.setArgs(args);
        //FIXME: I do not know the purpose of transient map works for.
        Map<String, byte[]> transientMap = new HashMap<>();
        Map<String, byte[]> tm2 = new HashMap<>();
        transientMap.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(Charset.forName("UTF8"))); //Just some extra junk in transient map
        transientMap.put("method", "InstantiateProposalRequest".getBytes(Charset.forName("UTF-8"))); // ditto
        tm2.put("result", ":)".getBytes(Charset.forName("UTF8")));  // This should be returned see chaincode why.
        tm2.put("event", "!".getBytes(Charset.forName("UTF8")));  //This should trigger an event see chaincode why.
        request.setTransientMap(transientMap);

        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        return channel.sendTransactionProposal(request);
    }

    public CompletableFuture<BlockEvent.TransactionEvent> sendTransactionToOrderer(String channelName, Collection<ProposalResponse> responses)
            throws TransactionException, FabricClientException, InvalidArgumentException {

        Channel channel = clientHelper.createChannel(channelName, Organization.DEFAULT_ORDERER_NAME, Context.getPeersName(channelName));
        return channel.sendTransaction(responses);
    }
}
