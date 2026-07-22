package com.seckill.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.seckill.gateway.client")
public class SeckillGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillGatewayApplication.class, args);
    }
}