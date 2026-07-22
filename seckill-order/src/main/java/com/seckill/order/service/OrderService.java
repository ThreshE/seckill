package com.seckill.order.service;

import com.seckill.common.entity.GoodsStock;
import com.seckill.common.entity.SeckillOrder;
import com.seckill.common.result.Result;
import com.seckill.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    public Result<String> createOrder(Long userId, Long goodsId, BigDecimal amount) {
        SeckillOrder order = new SeckillOrder();
        order.setId(System.currentTimeMillis());
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setAmount(amount);
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return Result.success(order.getOrderNo());
    }
}