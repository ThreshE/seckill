package com.seckill.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.seckill.order", "com.seckill.common"})
@EnableDiscoveryClient
@MapperScan("com.seckill.order.mapper")
public class SeckillOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillOrderApplication.class, args);
    }
}