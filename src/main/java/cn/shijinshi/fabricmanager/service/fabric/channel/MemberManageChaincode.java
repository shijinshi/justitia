package cn.shijinshi.fabricmanager.service.fabric.channel;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChaincodeHelper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class MemberManageChaincode {
    private static final Logger LOGGER = Logger.getLogger(MemberManageChaincode.class);
    private final ChaincodeHelper chaincodeHelper;

    @Value("${fabric.channel.manage.chaincode}")
    private String memberManageChaincodeName;
    @Value("${fabric.submit-tx.wait-time}")
    private long waitTime;


    @Autowired
    public MemberManageChaincode(ChaincodeHelper chaincodeHelper) {
        this.chaincodeHelper = chaincodeHelper;
    }


    public enum RequestState {
        INVALID("invalid"),
        END("end"),
        ALL("");

        private String state;

        RequestState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }

    public enum RequestType {
        ADD_MEMBER("addMember"),
        DELETE_MEMBER("deleteMember"),
        MODIFY_ORG_CONFIG("modifyOrgConfig");

        private String type;

        RequestType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * 发起更改通道配置的请求
     *
     * @param channelId          通道ID
     * @param updatedConfigBytes 通道配置更新交易
     * @param requestType        请求类型
     * @param configVersion      当前的通道配置版本
     * @param description        请求内容描述信息
     */
    public String signRequests(String channelId, byte[] updatedConfigBytes, RequestType requestType, String configVersion, String description, Set<String> orgsMsp) throws MemberManageException {
        ArrayList<String> args = new ArrayList<>();
        Organization organization = Context.getOrganization();
        String requestId = channelId + organization.getOrgMspId() + System.currentTimeMillis();
        requestId = DatatypeConverter.printHexBinary(requestId.getBytes()).toLowerCase();
        args.add(requestId);
        args.add(Base64.getEncoder().encodeToString(updatedConfigBytes));
        args.add(requestType.getType());
        args.add(configVersion);
        args.add(description);
        args.add(StringUtils.join(orgsMsp, ","));

        Collection<ProposalResponse> responses = endorsement(channelId, "signRequests", args);
        submit2Orderer(channelId, responses);
        return requestId;
    }

    /**
     * 应答通道内其他成员发起的签名请求
     *
     * @param channelId 通道ID
     * @param requestId 请求ID
     * @param requester 请求发起人
     * @param reject    是否拒绝
     * @param signature 自己的签名数据
     * @param reason    如果拒绝，拒绝的原因
     */
    public void signResponses(String channelId, String requestId, String requester, boolean reject, byte[] signature, String reason) throws MemberManageException {
        ArrayList<String> args = new ArrayList<>();
        args.add(requestId);
        args.add(requester);
        if (reject) {
            args.add("Y");
        } else {
            args.add("N");
        }
        args.add(Base64.getEncoder().encodeToString(signature));
        args.add(reason);

        Collection<ProposalResponse> responses = endorsement(channelId, "signResponses", args);
        submit2Orderer(channelId, responses);
    }

    /**
     * 更改自己发起的签名申请的状态
     *
     * @param channelId 通道ID
     * @param requestId 请求ID
     * @param state     更改后的状态
     */
    public void updateRequestState(String channelId, String requestId, RequestState state) throws MemberManageException {
        ArrayList<String> args = new ArrayList<>();
        args.add(requestId);
        args.add(state.getState());

        Collection<ProposalResponse> responses = endorsement(channelId, "updateRequestState", args);
        submit2Orderer(channelId, responses);
    }

    /**
     * 查询自己发起的签名申请
     *
     * @param channelId 通道ID
     * @param state     请求状态，""表示全匹配
     */
    public List<SignRequest> getMyRequests(String channelId, String state) throws MemberManageException {
        ArrayList<String> args = new ArrayList<>();
        args.add(state);
        return queryChaincode(channelId, "getMySignRequests", args, SignRequest.class);
    }

    /**
     * 查询自己发起的签名申请的所有签名应答
     *
     * @param channelId 通道ID
     * @param requestId 请求ID
     */
    public List<SignResponse> getAllSignResponses(String channelId, String requestId) throws MemberManageException {
        ArrayList<String> args = new ArrayList<>();
        args.add(requestId);
        return queryChaincode(channelId, "getAllSignResponsesById", args, SignResponse.class);
    }

    /**
     * 查询当前处于signing状态的全部请求
     *
     * @param channelId 通道ID
     */
    @Deprecated
    public List<SignRequest> getAllSigningRequest(String channelId) throws MemberManageException {
        return queryChaincode(channelId, "getAllSigningRequest", null, SignRequest.class);
    }

    /**
     * 查询自己签名过的全部请求
     *
     * @param channelId 通道ID
     */
    public List<SignResponse> getAllSignResponsesByChannel(String channelId) throws MemberManageException {
        return queryChaincode(channelId, "getMyAllSignResponses", null, SignResponse.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T queryChaincode(String channelId, String function, ArrayList<String> args, Class responseType) throws MemberManageException {
        Collection<ProposalResponse> responses = endorsement(channelId, function, args);
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                FabricProposalResponse.Response fabricProposalResponse = response.getProposalResponse().getResponse();
                if (fabricProposalResponse != null && fabricProposalResponse.getStatus() == 200) {
                    ByteString payload = fabricProposalResponse.getPayload();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, responseType);
                    try {
                        return (T) objectMapper.readValue(payload.toByteArray(), javaType);
                    } catch (IOException e) {
                        LOGGER.warn(e);
                    }
                }
            }
        }
        throw new MemberManageException(String.format("Chaincode function %s of %s call failed.", function, memberManageChaincodeName));
    }





    private Collection<ProposalResponse> endorsement(String channelId, String function, ArrayList<String> args) throws MemberManageException {
        try {
            return chaincodeHelper.invokeChaincode(channelId, memberManageChaincodeName, function, args);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new MemberManageException(String.format("Chaincode function %s of %s endorsement failed.", function, memberManageChaincodeName));
        }
    }

    private void submit2Orderer(String channelId, Collection<ProposalResponse> responses) throws MemberManageException {
        Collection<ProposalResponse> successful = new LinkedList<>();
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
            }
        }
        if (!successful.isEmpty()) {
            CompletableFuture<BlockEvent.TransactionEvent> future;
            try {
                future = chaincodeHelper.sendTransactionToOrderer(channelId, successful);
            } catch (Exception e) {
                LOGGER.error(e);
                throw new MemberManageException("Transaction failed to be submitted");
            }
            try {
                BlockEvent.TransactionEvent transactionEvent = future.get(waitTime, TimeUnit.MILLISECONDS);
                if (!transactionEvent.isValid()) {
                    throw new MemberManageException("Transaction commit failed: invalid transaction.");
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e);
                throw new MemberManageException("Waiting for the result exit. Not sure if the transaction is in effect.");
            } catch (TimeoutException e) {
                LOGGER.error(e);
                throw new MemberManageException("Wait result timed out.");
            }
        } else {
            String reason;
            if (responses.isEmpty()) {
                reason = "Endorsement result is empty.";
            } else {
                reason = responses.iterator().next().getMessage();
            }
            throw new MemberManageException("Transaction endorsement failed, Did not get a valid endorsement." + reason);
        }
    }


    public static class SignRequest {
        private String id;
        private String from;
        private String content;
        private String[] msp;
        private String desc;
        private long version;
        private String status;
        private long time;
        private String type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String[] getMsp() {
            return msp;
        }

        public void setMsp(String[] msp) {
            this.msp = msp;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class SignResponse {
        private String id;
        private String reject;
        private String from;
        private String to;
        private String signature;
        private String reason;
        private long time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getReject() {
            return reject;
        }

        public void setReject(String reject) {
            this.reject = reject;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }

}
