package com.seckill.order.controller;

import com.seckill.common.result.Result;
import com.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<String> createOrder(@RequestParam("userId") Long userId,
                                       @RequestParam("goodsId") Long goodsId,
                                       @RequestParam("amount") BigDecimal amount) {
        return orderService.createOrder(userId, goodsId, amount);
    }
}