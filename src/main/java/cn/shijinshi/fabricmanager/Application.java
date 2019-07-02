package cn.shijinshi.fabricmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableRetry
@EnableTransactionManagement
@SpringBootApplication
@MapperScan("cn.shijinshi.fabricmanager.dao.mapper")
public class Application {

    public static void main (String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
