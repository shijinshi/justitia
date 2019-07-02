package cn.shijinshi.fabricmanager.controller.entity.user;

import javax.validation.constraints.NotEmpty;

public class UserRemarkEntity {
    @NotEmpty
    private String userId;
    @NotEmpty
    private String remark;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
