package com.seckill.service.feign;

import com.seckill.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "seckill-stock")
public interface StockFeignClient {

    @PostMapping("/stock/decrease")
    Result<String> decrease(@RequestParam("goodsId") Long goodsId);

    @PostMapping("/stock/decrease/v2")
    Result<String> decreaseV2(@RequestParam("goodsId") Long goodsId);
}
