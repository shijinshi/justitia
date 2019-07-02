package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import cn.shijinshi.fabricmanager.service.ca.entity.EnrollExtend;

import javax.validation.constraints.NotNull;

public class EnrollEntity {
    private EnrollExtend enrollInfo;

    @NotNull
    private Boolean download;


    public EnrollExtend getEnrollInfo() {
        return enrollInfo;
    }

    public void setEnrollInfo(EnrollExtend enrollInfo) {
        this.enrollInfo = enrollInfo;
    }

    public Boolean getDownload() {
        return download;
    }

    public void setDownload(Boolean download) {
        this.download = download;
    }
}
