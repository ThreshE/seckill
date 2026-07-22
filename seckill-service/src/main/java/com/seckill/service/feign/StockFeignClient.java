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

    @PostMapping("/stock/decrease/v3-1")
    Result<String> decreaseV31(@RequestParam("goodsId") Long goodsId);

    @PostMapping("/stock/decrease/v3-2")
    Result<String> decreaseV32(@RequestParam("goodsId") Long goodsId);

    @PostMapping("/stock/decrease/v3-3")
    Result<String> decreaseV33(@RequestParam("goodsId") Long goodsId);

    @PostMapping("/stock/decrease/v3-4")
    Result<String> decreaseV34(@RequestParam("goodsId") Long goodsId);

    @PostMapping("/stock/decrease/v3-5")
    Result<String> decreaseV35(@RequestParam("goodsId") Long goodsId);
}
