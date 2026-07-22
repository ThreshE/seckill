package com.seckill.service.controller;

import com.seckill.common.result.Result;
import com.seckill.service.feign.OrderFeignClient;
import com.seckill.service.feign.PaymentFeignClient;
import com.seckill.service.feign.StockFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final StockFeignClient stockFeignClient;
    private final OrderFeignClient orderFeignClient;
    private final PaymentFeignClient paymentFeignClient;

    @PostMapping("/buy")
    public Result<String> buy(@RequestParam("userId") Long userId,
                               @RequestParam("goodsId") Long goodsId) {
        // 1. 扣库存
        Result<String> stockResult = stockFeignClient.decrease(goodsId);
        if (stockResult.getCode() != 200) {
            return stockResult;
        }

        // 2. 查商品价格（模拟：hardcode 7999）
        java.math.BigDecimal amount = new java.math.BigDecimal("7999");

        // 3. 扣余额
        Result<String> payResult = paymentFeignClient.pay(userId, amount);
        if (payResult.getCode() != 200) {
            return payResult;
        }

        // 4. 创建订单
        Result<String> orderResult = orderFeignClient.createOrder(userId, goodsId, amount);
        if (orderResult.getCode() != 200) {
            return orderResult;
        }

        return Result.success("抢购成功");
    }
}