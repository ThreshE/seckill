package com.seckill.service.feign;

import com.seckill.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "seckill-order")
public interface OrderFeignClient {

    @PostMapping("/order/create")
    Result<String> createOrder(@RequestParam("userId") Long userId,
                                @RequestParam("goodsId") Long goodsId,
                                @RequestParam("amount") BigDecimal amount);
}