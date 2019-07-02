package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import com.github.dockerjava.api.DockerClient;
import org.apache.log4j.Logger;

public class DockerHelper {
    final Logger LOGGER = Logger.getLogger(ImageHelper.class);

    protected DockerClient client;

    DockerHelper(DockerClient client) {
        this.client = client;
    }

    /**
     * 释放DockerClient占用资源
     */
    public void close(){
        try {
            client.close();
        }catch (Exception e) {
            LOGGER.warn(e);
        }
    }
}
