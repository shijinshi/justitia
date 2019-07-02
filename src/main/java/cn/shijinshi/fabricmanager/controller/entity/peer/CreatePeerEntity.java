package cn.shijinshi.fabricmanager.controller.entity.peer;

import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotEmpty;

public class CreatePeerEntity extends CreateContainerEntity {
    @NotEmpty
    private String peerName;
    private int serverPort;

    /**
     * couchdb
     */
    private Boolean couchdbEnable;
    private String couchdbImage;
    private String couchdbTag;
    private String couchdbContainerName;
    private Integer couchdbExposedPort;

    /**
     * msp
     */
    @NotEmpty
    private String caServerName;
    @NotEmpty
    private String peerUserId;

    @Autowired
    public String getImage() {
        if (StringUtils.isEmpty(image)) {
            image = "hyperledger/fabric-peer";
        }
        return image;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public Boolean getCouchdbEnable() {
        return couchdbEnable;
    }

    public void setCouchdbEnable(Boolean couchdbEnable) {
        this.couchdbEnable = couchdbEnable;
    }

    public String getCouchdbImage() {
        return couchdbImage;
    }

    public void setCouchdbImage(String couchdbImage) {
        this.couchdbImage = couchdbImage;
    }

    public String getCouchdbTag() {
        return couchdbTag;
    }

    public void setCouchdbTag(String couchdbTag) {
        this.couchdbTag = couchdbTag;
    }

    public String getCouchdbContainerName() {
        return couchdbContainerName;
    }

    public void setCouchdbContainerName(String couchdbContainerName) {
        this.couchdbContainerName = couchdbContainerName;
    }

    public Integer getCouchdbExposedPort() {
        return couchdbExposedPort;
    }

    public void setCouchdbExposedPort(Integer couchdbExposedPort) {
        this.couchdbExposedPort = couchdbExposedPort;
    }

    public String getCaServerName() {
        return caServerName;
    }

    public void setCaServerName(String caServerName) {
        this.caServerName = caServerName;
    }

    public String getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(String peerUserId) {
        this.peerUserId = peerUserId;
    }
}
