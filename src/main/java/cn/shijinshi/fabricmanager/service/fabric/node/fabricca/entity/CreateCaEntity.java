package cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity;

import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;
import cn.shijinshi.fabricmanager.controller.entity.fabricca.manage.SetCaServerEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateCaEntity extends CreateContainerEntity {
    @NotEmpty
    private String serverName;

    @NotNull
    private SetCaServerEntity serverConfig;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public SetCaServerEntity getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(SetCaServerEntity serverConfig) {
        this.serverConfig = serverConfig;
    }
}
