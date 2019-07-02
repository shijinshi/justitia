package cn.shijinshi.fabricmanager.controller.entity.fabricca.manage;

import java.util.Map;

public class SetCaServerEntity {
    private Integer serverPort;
    private String user;
    private String userPassword;
    private Boolean debug;
    private String crlExpiry;
    private Map affiliations;
    private String certExpiry;
    private Map csrName;
    private String cn;

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getCrlExpiry() {
        return crlExpiry;
    }

    public void setCrlExpiry(String crlExpiry) {
        this.crlExpiry = crlExpiry;
    }

    public Map getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(Map affiliations) {
        this.affiliations = affiliations;
    }

    public String getCertExpiry() {
        return certExpiry;
    }

    public void setCertExpiry(String certExpiry) {
        this.certExpiry = certExpiry;
    }

    public Map getCsrName() {
        return csrName;
    }

    public void setCsrName(Map csrName) {
        this.csrName = csrName;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
