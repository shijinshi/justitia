package cn.shijinshi.fabricmanager.service.fabric.host;

import cn.shijinshi.fabricmanager.Application;
import cn.shijinshi.fabricmanager.controller.entity.docker.CreateContainerEntity;
import cn.shijinshi.fabricmanager.service.DockerService;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
public class DockerServiceTest {
    private String hostName = "testHost";

    @Autowired
    private DockerService dockerService;


    @Test
    public void createContainer() {
        CreateContainerEntity config = new CreateContainerEntity();

        CreateContainerResponse response = dockerService.createContainer(config);
        System.out.println(response.toString());
    }
}