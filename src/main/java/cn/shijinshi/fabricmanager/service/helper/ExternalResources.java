package cn.shijinshi.fabricmanager.service.helper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service("ExternalResources")
public class ExternalResources implements InitializingBean {
    @Value("${external.fabric-ca-server}")
    private String fabricCaServer_temp;

    @Value("${external.resources.temp}")
    private String temp_temp;

    @Value("${external.resources.fabricca}")
    private String fabricca_temp;

    @Value("${external.resources.docker-certs}")
    private String docker_certs_temp;

    @Value("${external.resources.scripts}")
    private String scripts_temp;


    private static String fabricCaServer;
    private static String temp;
    private static String dockerCerts;
    private static String fabricca;
    private static String scripts;

    @Override
    public void afterPropertiesSet() {
        String userDir = System.getProperty("user.dir");
        fabricCaServer = userDir + fabricCaServer_temp.replace("/", File.separator);
        temp = userDir + temp_temp.replace("/", File.separator);
        dockerCerts = userDir + docker_certs_temp.replace("/", File.separator);
        fabricca = userDir + fabricca_temp.replace("/", File.separator);
        scripts = userDir + scripts_temp.replace("/", File.separator);
    }

    public static String getFabricCaServer() {
        return fabricCaServer;
    }

    public static String getFabricCaServer(String subPath) {
        if (subPath == null || subPath.isEmpty()) {
            return fabricCaServer;
        } else {
            return fabricCaServer + subPath.replace("/", File.separator);
        }
    }

    public static String getTemp() {
        return temp;
    }

    public static String getTemp(String subPath) {
        if (subPath == null || "".equals(subPath)) {
            return temp;
        } else {
            return temp + subPath.replace("/", File.separator);
        }
    }

    public static String getUniqueTempDir(){
        int random = (int) (Math.random() * 1000);
        String dirName = "" + System.currentTimeMillis() + random;
        return getTemp(dirName);
    }

    public static String getDockerCerts() {
        return dockerCerts;
    }

    public static String getDockerCerts(String subPath) {
        if (subPath == null || "".equals(subPath)) {
            return dockerCerts;
        } else {
            return dockerCerts + subPath.replace("/", File.separator);
        }
    }

    public static String getFabricca() {
        return fabricca;
    }

    public static String getFabricca(String subPath) {
        if (subPath == null || "".equals(subPath)) {
            return fabricca;
        } else {
            return fabricca + subPath.replace("/", File.separator);
        }
    }

    public static String getScripts() {
        return scripts;
    }

    public static String getScripts(String subPath) {
        if (subPath == null || "".equals(subPath)) {
            return scripts;
        } else {
            return scripts + subPath.replace("/", File.separator);
        }
    }

}
