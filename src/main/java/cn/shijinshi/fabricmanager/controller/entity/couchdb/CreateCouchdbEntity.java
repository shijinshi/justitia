package cn.shijinshi.fabricmanager.controller.entity.couchdb;

import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateCouchdbEntity extends CreateContainerEntity {

    @NotEmpty
    private String couchdbName;
    @NotNull
    private Integer serverPort;

    public String getCouchdbName() {
        return couchdbName;
    }

    public void setCouchdbName(String couchdbName) {
        this.couchdbName = couchdbName;
    }

    public Integer getServerPort() {
        if (serverPort == null || serverPort == 0) {
            return 5984;
        }
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
