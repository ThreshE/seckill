package com.seckill.gateway.exception;

import com.seckill.common.dto.SeckillBuyResponse;
import feign.FeignException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FeignException.class)
    public SeckillBuyResponse handleFeignException(FeignException e) {
        return new SeckillBuyResponse(false, "服务调用失败：" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public SeckillBuyResponse handleException(Exception e) {
        return new SeckillBuyResponse(false, "系统异常：" + e.getMessage());
    }
}
