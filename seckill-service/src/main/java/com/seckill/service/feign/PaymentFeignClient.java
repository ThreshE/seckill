package com.seckill.service.feign;

import com.seckill.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "seckill-payment")
public interface PaymentFeignClient {

    @PostMapping("/payment/pay")
    Result<String> pay(@RequestParam("userId") Long userId,
                        @RequestParam("amount") BigDecimal amount);
}