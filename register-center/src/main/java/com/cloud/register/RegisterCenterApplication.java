package com.cloud.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 注册中心<br>
 * 访问地址 http://localhost:8761
 *
 * @author
 */
@EnableEurekaServer  // 标记此项目为eureka服务端 注册中心
@SpringBootApplication
public class RegisterCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegisterCenterApplication.class, args);
    }

}