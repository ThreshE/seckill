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

import java.math.BigDecimal;
import java.util.function.Function;

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

    @PostMapping("/buy/v3-1")
    public Result<String> buyV31(@RequestParam("userId") Long userId,
                                  @RequestParam("goodsId") Long goodsId) {
        return buySync("V3-1-DB-Atomic", userId, goodsId, stockFeignClient::decreaseV31);
    }

    @PostMapping("/buy/v3-2")
    public Result<String> buyV32(@RequestParam("userId") Long userId,
                                  @RequestParam("goodsId") Long goodsId) {
        return buySync("V3-2-DB-Optimistic", userId, goodsId, stockFeignClient::decreaseV32);
    }

    @PostMapping("/buy/v3-3")
    public Result<String> buyV33(@RequestParam("userId") Long userId,
                                  @RequestParam("goodsId") Long goodsId) {
        return buySync("V3-3-Redis-Lock", userId, goodsId, stockFeignClient::decreaseV33);
    }

    @PostMapping("/buy/v3-4")
    public Result<String> buyV34(@RequestParam("userId") Long userId,
                                  @RequestParam("goodsId") Long goodsId) {
        String traceId = TraceUtil.getTraceId();
        String threadName = Thread.currentThread().getName();

        log.info("===== [{}][{}] 秒杀请求开始[V3-4-MQ-Serial]: userId={}, goodsId={}", traceId, threadName, userId, goodsId);
        Result<String> stockResult = stockFeignClient.decreaseV34(goodsId);
        log.info("[{}][{}] V3-4 库存入队结果={}", traceId, threadName, stockResult);
        return stockResult;
    }

    @PostMapping("/buy/v3-5")
    public Result<String> buyV35(@RequestParam("userId") Long userId,
                                  @RequestParam("goodsId") Long goodsId) {
        return buySync("V3-5-Redis-PreDeduct", userId, goodsId, stockFeignClient::decreaseV35);
    }

    private Result<String> buySync(String version,
                                   Long userId,
                                   Long goodsId,
                                   Function<Long, Result<String>> stockDecrease) {
        String traceId = TraceUtil.getTraceId();
        String threadName = Thread.currentThread().getName();

        log.info("===== [{}][{}] 秒杀请求开始[{}]: userId={}, goodsId={}", traceId, threadName, version, userId, goodsId);

        long start = System.currentTimeMillis();

        log.info("[{}][{}] Step1: 调用库存服务扣减库存[{}] goodsId={}", traceId, threadName, version, goodsId);
        Result<String> stockResult = stockDecrease.apply(goodsId);
        log.info("[{}][{}] Step1: 库存结果[{}]={}", traceId, threadName, version, stockResult);

        if (stockResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 秒杀失败[{}]: {} (耗时={}ms)", traceId, threadName, version, stockResult.getMsg(), cost);
            return stockResult;
        }

        BigDecimal amount = new BigDecimal("7999");

        log.info("[{}][{}] Step2: 调用支付服务扣减余额[{}] userId={}, amount={}", traceId, threadName, version, userId, amount);
        Result<String> payResult = paymentFeignClient.pay(userId, amount);
        if (payResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 支付失败[{}]: {} (耗时={}ms)", traceId, threadName, version, payResult.getMsg(), cost);
            return payResult;
        }

        log.info("[{}][{}] Step3: 调用订单服务创建订单[{}]", traceId, threadName, version);
        Result<String> orderResult = orderFeignClient.createOrder(userId, goodsId, amount);
        if (orderResult.getCode() != 200) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}][{}] 订单创建失败[{}]: {} (耗时={}ms)", traceId, threadName, version, orderResult.getMsg(), cost);
            return orderResult;
        }

        long cost = System.currentTimeMillis() - start;
        log.info("===== [{}][{}] 秒杀成功[{}]! 订单号={} (耗时={}ms)", traceId, threadName, version, orderResult.getData(), cost);
        return Result.success(orderResult.getData(), "抢购成功");
    }
}
