package com.cloud.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 文件中心
 * 
 * @author
 *
 */
@EnableEurekaClient
@SpringBootApplication
public class FileCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileCenterApplication.class, args);
	}

}