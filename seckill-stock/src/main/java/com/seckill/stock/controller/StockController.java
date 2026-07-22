package com.seckill.stock.controller;

import com.seckill.common.result.Result;
import com.seckill.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/decrease")
    public Result<String> decrease(@RequestParam("goodsId") Long goodsId) {
        return stockService.decrease(goodsId);
    }

    @PostMapping("/decrease/v2")
    public Result<String> decreaseV2(@RequestParam("goodsId") Long goodsId) {
        return stockService.decreaseV2(goodsId);
    }
}
