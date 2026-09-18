package com.ecommerce.reviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
        System.out.println("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║     REVIEW SERVICE                                           ║
            ║     Running on: http://localhost:8085                       ║
            ╚═══════════════════════════════════════════════════════════════╝
        """);
    }
}