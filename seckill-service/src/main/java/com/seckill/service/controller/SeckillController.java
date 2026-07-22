package com.seckill.service.controller;

import com.seckill.common.result.Result;
import com.seckill.common.utils.TraceUtil;
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
        String traceId = TraceUtil.getTraceId();
        String threadName = Thread.currentThread().getName();

        log.info("===== [{}][{}] 秒杀请求开始: userId={}, goodsId={}", traceId, threadName, userId, goodsId);

        long start = System.currentTimeMillis();

        // 1. 扣库存
        log.info("[{}][{}] Step1: 调用库存服务扣减库存 goodsId={}", traceId, threadName, goodsId);
        Result<String> stockResult = stockFeignClient.decrease(goodsId);
        log.info("[{}][{}] Step1: 库存结果={}", traceId, threadName, stockResult);

        if (stockResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 秒杀失败: {} (耗时={}ms)", traceId, threadName, stockResult.getMsg(), cost);
            return stockResult;
        }

        // 2. 查商品价格
        java.math.BigDecimal amount = new java.math.BigDecimal("7999");

        // 3. 扣余额
        log.info("[{}][{}] Step2: 调用支付服务扣减余额 userId={}, amount={}", traceId, threadName, userId, amount);
        Result<String> payResult = paymentFeignClient.pay(userId, amount);
        if (payResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 支付失败: {} (耗时={}ms)", traceId, threadName, payResult.getMsg(), cost);
            return payResult;
        }

        // 4. 创建订单
        log.info("[{}][{}] Step3: 调用订单服务创建订单", traceId, threadName);
        Result<String> orderResult = orderFeignClient.createOrder(userId, goodsId, amount);
        if (orderResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 订单创建失败: {} (耗时={}ms)", traceId, threadName, orderResult.getMsg(), cost);
            return orderResult;
        }

        long cost = System.currentTimeMillis() - start;
        log.info("===== [{}][{}] 秒杀成功! 订单号={} (耗时={}ms)", traceId, threadName, orderResult.getData(), cost);
        return Result.success(orderResult.getData(), "抢购成功");
    }

    @PostMapping("/buy/v2")
    public Result<String> buyV2(@RequestParam("userId") Long userId,
                                 @RequestParam("goodsId") Long goodsId) {
        String traceId = TraceUtil.getTraceId();
        String threadName = Thread.currentThread().getName();

        log.info("===== [{}][{}] 秒杀请求开始[V2-ReentrantLock]: userId={}, goodsId={}", traceId, threadName, userId, goodsId);

        long start = System.currentTimeMillis();

        // 1. 扣库存
        log.info("[{}][{}] Step1: 调用库存服务扣减库存[V2-ReentrantLock] goodsId={}", traceId, threadName, goodsId);
        Result<String> stockResult = stockFeignClient.decreaseV2(goodsId);
        log.info("[{}][{}] Step1: 库存结果[V2-ReentrantLock]={}", traceId, threadName, stockResult);

        if (stockResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 秒杀失败[V2-ReentrantLock]: {} (耗时={}ms)", traceId, threadName, stockResult.getMsg(), cost);
            return stockResult;
        }

        // 2. 查商品价格
        java.math.BigDecimal amount = new java.math.BigDecimal("7999");

        // 3. 扣余额
        log.info("[{}][{}] Step2: 调用支付服务扣减余额[V2-ReentrantLock] userId={}, amount={}", traceId, threadName, userId, amount);
        Result<String> payResult = paymentFeignClient.pay(userId, amount);
        if (payResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 支付失败[V2-ReentrantLock]: {} (耗时={}ms)", traceId, threadName, payResult.getMsg(), cost);
            return payResult;
        }

        // 4. 创建订单
        log.info("[{}][{}] Step3: 调用订单服务创建订单[V2-ReentrantLock]", traceId, threadName);
        Result<String> orderResult = orderFeignClient.createOrder(userId, goodsId, amount);
        if (orderResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 订单创建失败[V2-ReentrantLock]: {} (耗时={}ms)", traceId, threadName, orderResult.getMsg(), cost);
            return orderResult;
        }

        long cost = System.currentTimeMillis() - start;
        log.info("===== [{}][{}] 秒杀成功[V2-ReentrantLock]! 订单号={} (耗时={}ms)", traceId, threadName, orderResult.getData(), cost);
        return Result.success(orderResult.getData(), "抢购成功");
    }
}
