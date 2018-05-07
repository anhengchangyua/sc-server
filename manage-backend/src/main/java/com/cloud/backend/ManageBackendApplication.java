package com.cloud.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 管理后台
 * 
 * @author
 *
 */
@EnableEurekaClient
@SpringBootApplication
public class ManageBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManageBackendApplication.class, args);
	}

}