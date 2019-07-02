package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;
import java.util.Date;

public class ChannelConfigTaskResponse implements Serializable {
    /**
     * 。
    * <p> column ==>request_id</p>
     */
    private String requestId;

    /**
     * 。
    * <p> column ==>responder</p>
     */
    private String responder;

    /**
     * 。
    * <p> column ==>reject</p>
     */
    private Boolean reject;

    /**
     * 。
    * <p> column ==>response_time</p>
     */
    private Date responseTime;

    /**
     * 。
    * <p> column ==>reason</p>
     */
    private String reason;

    /**
     * 。
    * <p> column ==>signature</p>
     */
    private byte[] signature;

    /**
     * channel_config_task_response。
    * <p> table ==>ChannelConfigTaskResponse</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #requestId}
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置 。
     * @param requestId 
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取 。
     * @return {@link #responder}
     */
    public String getResponder() {
        return responder;
    }

    /**
     * 设置 。
     * @param responder 
     */
    public void setResponder(String responder) {
        this.responder = responder;
    }

    /**
     * 获取 。
     * @return {@link #reject}
     */
    public Boolean getReject() {
        return reject;
    }

    /**
     * 设置 。
     * @param reject 
     */
    public void setReject(Boolean reject) {
        this.reject = reject;
    }

    /**
     * 获取 。
     * @return {@link #responseTime}
     */
    public Date getResponseTime() {
        return responseTime;
    }

    /**
     * 设置 。
     * @param responseTime 
     */
    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * 获取 。
     * @return {@link #reason}
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置 。
     * @param reason 
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取 。
     * @return {@link #signature}
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * 设置 。
     * @param signature 
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}