package cn.shijinshi.fabricmanager.controller.entity.docker;

import org.json.JSONObject;

import java.util.Map;

public class CreateContainerEntity {
    protected String hostName;
    protected String image;
    protected String tag;
    protected String workingDir;
    private String containerName;
    protected Map<String, String> env;
    protected String cmd;
    private Map<Integer, Integer> exposedPorts;  //key:容器内部端口, value:映射出来的端口
    private String networkMode;
    protected Map<String, String> volumes;         //key:本地挂在路径或卷, value:容器内部路径
    private String[] extraHosts;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Map<Integer, Integer> getExposedPorts() {
        return exposedPorts;
    }

    public String getExposedPortsString() {
        return JSONObject.valueToString(exposedPorts);
    }

    public void setExposedPorts(Map<Integer, Integer> exposedPorts) {
        this.exposedPorts = exposedPorts;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    public Map<String, String> getVolumes() {
        return volumes;
    }

    public void setVolumes(Map<String, String> volumes) {
        this.volumes = volumes;
    }

    public String[] getExtraHosts() {
        return extraHosts;
    }

    public void setExtraHosts(String[] extraHosts) {
        this.extraHosts = extraHosts;
    }
}
