package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class Host implements Serializable {
    /**
     * 。
    * <p> column ==>host_name</p>
     */
    private String hostName;

    /**
     * 。
    * <p> column ==>protocol</p>
     */
    private String protocol;

    /**
     * 。
    * <p> column ==>ip</p>
     */
    private String ip;

    /**
     * 。
    * <p> column ==>port</p>
     */
    private Integer port;

    /**
     * 。
    * <p> column ==>tls_enable</p>
     */
    private Boolean tlsEnable;

    /**
     * 。
    * <p> column ==>cert_path</p>
     */
    private String certPath;

    /**
     * host。
    * <p> table ==>Host</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #hostName}
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * 设置 。
     * @param hostName 
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * 获取 。
     * @return {@link #protocol}
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置 。
     * @param protocol 
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取 。
     * @return {@link #ip}
     */
    public String getIp() {
        return ip;
    }

    /**
     * 设置 。
     * @param ip 
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 获取 。
     * @return {@link #port}
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置 。
     * @param port 
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 获取 。
     * @return {@link #tlsEnable}
     */
    public Boolean getTlsEnable() {
        return tlsEnable;
    }

    /**
     * 设置 。
     * @param tlsEnable 
     */
    public void setTlsEnable(Boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    /**
     * 获取 。
     * @return {@link #certPath}
     */
    public String getCertPath() {
        return certPath;
    }

    /**
     * 设置 。
     * @param certPath 
     */
    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public Host clone() {
        Host host = new Host();
        host.setHostName(this.hostName);
        host.setProtocol(this.protocol);
        host.setIp(this.ip);
        host.setPort(this.port);
        host.setTlsEnable(this.tlsEnable);
        host.setCertPath(this.certPath);
        return host;
    }
}