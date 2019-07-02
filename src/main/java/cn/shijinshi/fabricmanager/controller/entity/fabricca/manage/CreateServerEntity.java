package cn.shijinshi.fabricmanager.controller.entity.fabricca.manage;

import cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity.CreateCaEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateServerEntity {

    private CreateCaEntity containerConfig;
    @NotEmpty
    private String hostName;
    @NotEmpty
    private String serverName;
    @NotNull
    private int serverPort;
    @NotEmpty
    private String adminUser;
    @NotEmpty
    private String adminPassword;
    @NotEmpty
    private String serverType;
    @NotNull
    private Boolean uploadCert;
    private String parentServerName;
    private String parentUserId;

    private SetCaServerEntity serverConfig;

    public CreateCaEntity getContainerConfig() {
        return containerConfig;
    }

    public void setContainerConfig(CreateCaEntity containerConfig) {
        this.containerConfig = containerConfig;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public Boolean getUploadCert() {
        return uploadCert;
    }

    public void setUploadCert(Boolean uploadCert) {
        this.uploadCert = uploadCert;
    }

    public String getParentServerName() {
        return parentServerName;
    }

    public void setParentServerName(String parentServerName) {
        this.parentServerName = parentServerName;
    }

    public String getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }

    public SetCaServerEntity getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(SetCaServerEntity serverConfig) {
        this.serverConfig = serverConfig;
    }
}
