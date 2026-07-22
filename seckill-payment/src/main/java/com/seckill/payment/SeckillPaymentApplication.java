package com.seckill.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.seckill.payment", "com.seckill.common"})
@EnableDiscoveryClient
@MapperScan("com.seckill.payment.mapper")
public class SeckillPaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillPaymentApplication.class, args);
    }
}