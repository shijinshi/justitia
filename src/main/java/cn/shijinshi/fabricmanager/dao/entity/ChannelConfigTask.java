package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;
import java.util.Date;

public class ChannelConfigTask implements Serializable {
    /**
     * 。
    * <p> column ==>request_id</p>
     */
    private String requestId;

    /**
     * 。
    * <p> column ==>channel_id</p>
     */
    private String channelId;

    /**
     * 。
    * <p> column ==>requester</p>
     */
    private String requester;

    /**
     * 。
    * <p> column ==>channel_config_version</p>
     */
    private Long channelConfigVersion;

    /**
     * 。
    * <p> column ==>status</p>
     */
    private String status;

    /**
     * 。
    * <p> column ==>request_time</p>
     */
    private Date requestTime;

    /**
     * 。
    * <p> column ==>request_type</p>
     */
    private String requestType;

    /**
     * 。
    * <p> column ==>reject</p>
     */
    private Boolean reject;

    /**
     * 。
    * <p> column ==>reason</p>
     */
    private String reason;

    /**
     * 。
    * <p> column ==>response_time</p>
     */
    private Date responseTime;

    /**
     * 。
    * <p> column ==>content</p>
     */
    private byte[] content;

    /**
     * 。
    * <p> column ==>description</p>
     */
    private String description;

    /**
     * 。
    * <p> column ==>expected_endorsement</p>
     */
    private String expectedEndorsement;

    /**
     * channel_config_task。
    * <p> table ==>ChannelConfigTask</p>
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
     * @param requestId  {@link #requestId}
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取 。
     * @return {@link #channelId}
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * 设置 。
     * @param channelId  {@link #channelId}
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * 获取 。
     * @return {@link #requester}
     */
    public String getRequester() {
        return requester;
    }

    /**
     * 设置 。
     * @param requester  {@link #requester}
     */
    public void setRequester(String requester) {
        this.requester = requester;
    }

    /**
     * 获取 。
     * @return {@link #channelConfigVersion}
     */
    public Long getChannelConfigVersion() {
        return channelConfigVersion;
    }

    /**
     * 设置 。
     * @param channelConfigVersion  {@link #channelConfigVersion}
     */
    public void setChannelConfigVersion(Long channelConfigVersion) {
        this.channelConfigVersion = channelConfigVersion;
    }

    /**
     * 获取 。
     * @return {@link #status}
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置 。
     * @param status  {@link #status}
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取 。
     * @return {@link #requestTime}
     */
    public Date getRequestTime() {
        return requestTime;
    }

    /**
     * 设置 。
     * @param requestTime  {@link #requestTime}
     */
    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * 获取 。
     * @return {@link #requestType}
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * 设置 。
     * @param requestType  {@link #requestType}
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
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
     * @param reject  {@link #reject}
     */
    public void setReject(Boolean reject) {
        this.reject = reject;
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
     * @param reason  {@link #reason}
     */
    public void setReason(String reason) {
        this.reason = reason;
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
     * @param responseTime  {@link #responseTime}
     */
    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * 获取 。
     * @return {@link #content}
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * 设置 。
     * @param content  {@link #content}
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * 获取 。
     * @return {@link #description}
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置 。
     * @param description  {@link #description}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取 。
     * @return {@link #expectedEndorsement}
     */
    public String getExpectedEndorsement() {
        return expectedEndorsement;
    }

    /**
     * 设置 。
     * @param expectedEndorsement  {@link #expectedEndorsement}
     */
    public void setExpectedEndorsement(String expectedEndorsement) {
        this.expectedEndorsement = expectedEndorsement;
    }
}