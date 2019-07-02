package cn.shijinshi.fabricmanager.controller.entity.channel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ResponseTaskEntity {
    @NotEmpty
    private String taskId;
    @NotNull
    private Boolean reject;
    private String reason;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Boolean isReject() {
        return reject;
    }

    public void setReject(Boolean reject) {
        this.reject = reject;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
