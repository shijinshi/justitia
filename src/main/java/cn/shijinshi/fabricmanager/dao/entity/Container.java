package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class Container implements Serializable {
    /**
     * 。
    * <p> column ==>host_name</p>
     */
    private String hostName;

    /**
     * 。
    * <p> column ==>container_id</p>
     */
    private String containerId;

    /**
     * 。
    * <p> column ==>container_name</p>
     */
    private String containerName;

    /**
     * 。
    * <p> column ==>image</p>
     */
    private String image;

    /**
     * 。
    * <p> column ==>tag</p>
     */
    private String tag;

    /**
     * 。
    * <p> column ==>working_dir</p>
     */
    private String workingDir;

    /**
     * 。
    * <p> column ==>network_mode</p>
     */
    private String networkMode;

    /**
     * 。
    * <p> column ==>exposed_port</p>
     */
    private String exposedPort;

    /**
     * 。
    * <p> column ==>status</p>
     */
    private String status;

    /**
     * 。
    * <p> column ==>volumes</p>
     */
    private String volumes;

    /**
     * container。
    * <p> table ==>Container</p>
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
     * @return {@link #containerId}
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * 设置 。
     * @param containerId 
     */
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    /**
     * 获取 。
     * @return {@link #containerName}
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * 设置 。
     * @param containerName 
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * 获取 。
     * @return {@link #image}
     */
    public String getImage() {
        return image;
    }

    /**
     * 设置 。
     * @param image 
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * 获取 。
     * @return {@link #tag}
     */
    public String getTag() {
        return tag;
    }

    /**
     * 设置 。
     * @param tag 
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取 。
     * @return {@link #workingDir}
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * 设置 。
     * @param workingDir 
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * 获取 。
     * @return {@link #networkMode}
     */
    public String getNetworkMode() {
        return networkMode;
    }

    /**
     * 设置 。
     * @param networkMode 
     */
    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    /**
     * 获取 。
     * @return {@link #exposedPort}
     */
    public String getExposedPort() {
        return exposedPort;
    }

    /**
     * 设置 。
     * @param exposedPort 
     */
    public void setExposedPort(String exposedPort) {
        this.exposedPort = exposedPort;
    }

    /**
     * 获取 。
     * @return {@link #status}
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置 。
     * @param status 
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取 。
     * @return {@link #volumes}
     */
    public String getVolumes() {
        return volumes;
    }

    /**
     * 设置 。
     * @param volumes 
     */
    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }
}