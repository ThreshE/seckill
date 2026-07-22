package com.seckill.payment.controller;

import com.seckill.common.result.Result;
import com.seckill.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam("userId") Long userId,
                               @RequestParam("amount") BigDecimal amount) {
        return paymentService.pay(userId, amount);
    }
}