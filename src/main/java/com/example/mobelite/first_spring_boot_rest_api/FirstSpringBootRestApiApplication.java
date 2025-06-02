package com.example.mobelite.first_spring_boot_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FirstSpringBootRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirstSpringBootRestApiApplication.class, args);
		System.out.println("Hello World");
	}


}