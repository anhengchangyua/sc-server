package com.cloud.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * 认证中心
 * 
 * @author jh xiaoweijiagou@163.com
 *
 */
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class OAuthCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuthCenterApplication.class, args);
	}

}