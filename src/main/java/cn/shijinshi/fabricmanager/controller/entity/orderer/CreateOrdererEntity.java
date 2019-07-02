package cn.shijinshi.fabricmanager.controller.entity.orderer;

import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotEmpty;

public class CreateOrdererEntity extends CreateContainerEntity {
    @NotEmpty
    private String ordererName;
    private int serverPort;
    @NotEmpty
    private String systemChainId;
    @NotEmpty
    private String consortiumName;

    /**
     * msp
     */
    @NotEmpty
    private String caServerName;
    @NotEmpty
    private String ordererUserId;


    @Autowired
    public String getImage() {
        if (StringUtils.isEmpty(image)) {
            image = "hyperledger/fabric-orderer";
        }
        return image;
    }

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getCaServerName() {
        return caServerName;
    }

    public void setCaServerName(String caServerName) {
        this.caServerName = caServerName;
    }

    public String getOrdererUserId() {
        return ordererUserId;
    }

    public void setOrdererUserId(String ordererUserId) {
        this.ordererUserId = ordererUserId;
    }

    public String getSystemChainId() {
        return systemChainId;
    }

    public void setSystemChainId(String systemChainId) {
        this.systemChainId = systemChainId;
    }

    public String getConsortiumName() {
        return consortiumName;
    }

    public void setConsortiumName(String consortiumName) {
        this.consortiumName = consortiumName;
    }
}
