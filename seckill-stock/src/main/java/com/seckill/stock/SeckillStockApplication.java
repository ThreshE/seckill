package com.seckill.stock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.seckill.stock", "com.seckill.common"})
@EnableDiscoveryClient
@MapperScan("com.seckill.stock.mapper")
public class SeckillStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillStockApplication.class, args);
    }
}