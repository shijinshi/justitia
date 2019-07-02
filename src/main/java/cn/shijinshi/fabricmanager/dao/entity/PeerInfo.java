package cn.shijinshi.fabricmanager.dao.entity;

import java.util.Properties;

public class PeerInfo {
    private String peerName;
    private int serverPort;
    private String ip;
    private boolean tlsEnable;
    private String tlsCert;
    private String exposedPort;

    private String url;
    private Properties properties;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isTlsEnable() {
        return tlsEnable;
    }

    public void setTlsEnable(boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    public String getTlsCert() {
        return tlsCert;
    }

    public void setTlsCert(String tlsCert) {
        this.tlsCert = tlsCert;
    }

    public String getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(String exposedPort) {
        this.exposedPort = exposedPort;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
