package com.seckill.gateway.controller;

import com.seckill.common.dto.SeckillBuyRequest;
import com.seckill.common.dto.SeckillBuyResponse;
import com.seckill.gateway.client.SeckillServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
public class GatewaySeckillController {
    @Autowired
    private SeckillServiceClient seckillServiceClient;

    /**
     * 秒杀购买接口 - Gateway 入口
     * 通过 Feign 调用 Service 的 /api/seckill/buy 方法
     */
    @PostMapping("/buy")
    public SeckillBuyResponse buy(@RequestBody SeckillBuyRequest request) {
        return seckillServiceClient.buy(request);
    }
}
