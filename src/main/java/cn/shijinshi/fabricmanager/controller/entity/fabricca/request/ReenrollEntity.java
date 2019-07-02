package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import cn.shijinshi.fabricmanager.service.ca.entity.EnrollExtend;

import javax.validation.constraints.NotNull;

public class ReenrollEntity {

    private EnrollExtend enrollInfo;

    @NotNull
    private Boolean download;

//    private boolean usePem;
//    private String certPem;
//    private String keyPem;
//    private String serial;
//    private String aki;


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

//    public boolean isUsePem() {
//        return usePem;
//    }
//
//    public void setUsePem(boolean usePem) {
//        this.usePem = usePem;
//    }
//
//    public String getCertPem() {
//        return certPem;
//    }
//
//    public void setCertPem(String certPem) {
//        this.certPem = certPem;
//    }
//
//    public String getKeyPem() {
//        return keyPem;
//    }
//
//    public void setKeyPem(String keyPem) {
//        this.keyPem = keyPem;
//    }
//
//    public String getSerial() {
//        return serial;
//    }
//
//    public void setSerial(String serial) {
//        this.serial = serial;
//    }
//
//    public String getAki() {
//        return aki;
//    }
//
//    public void setAki(String aki) {
//        this.aki = aki;
//    }
}
