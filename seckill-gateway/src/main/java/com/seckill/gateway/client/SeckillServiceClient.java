package com.seckill.gateway.client;

import com.seckill.common.dto.SeckillBuyRequest;
import com.seckill.common.dto.SeckillBuyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seckill-service", path = "/api/seckill")
public interface SeckillServiceClient {
    /**
     * 调用 Service 的秒杀购买接口
     */
    @PostMapping("/buy")
    SeckillBuyResponse buy(@RequestBody SeckillBuyRequest request);
}
