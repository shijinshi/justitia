package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class Remark implements Serializable {
    /**
     * 。
    * <p> column ==>parent_user_id</p>
     */
    private String parentUserId;

    /**
     * 。
    * <p> column ==>user_id</p>
     */
    private String userId;

    /**
     * 。
    * <p> column ==>remarks</p>
     */
    private String remarks;

    /**
     * remark。
    * <p> table ==>Remark</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #parentUserId}
     */
    public String getParentUserId() {
        return parentUserId;
    }

    /**
     * 设置 。
     * @param parentUserId 
     */
    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }

    /**
     * 获取 。
     * @return {@link #userId}
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置 。
     * @param userId 
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 。
     * @return {@link #remarks}
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * 设置 。
     * @param remarks 
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}