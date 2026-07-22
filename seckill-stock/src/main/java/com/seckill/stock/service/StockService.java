package com.seckill.stock.service;

import com.seckill.common.entity.GoodsStock;
import com.seckill.common.result.Result;
import com.seckill.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;

    /**
     * V1: 使用 synchronized 保证库存扣减的原子性
     * 解决 V0 的超卖和更新丢失问题
     * 局限：仅单机有效，多实例部署时仍会超卖
     */
    public synchronized Result<String> decrease(Long goodsId) {
        String threadName = Thread.currentThread().getName();

        GoodsStock stock = stockMapper.selectById(goodsId);
        if (stock == null) {
            log.warn("[{}] 商品不存在 goodsId={}", threadName, goodsId);
            return Result.fail("商品不存在");
        }

        int beforeStock = stock.getStock();
        log.info("[{}] 读库存: goodsId={}, 当前库存={}", threadName, goodsId, beforeStock);

        if (beforeStock <= 0) {
            log.warn("[{}] 库存不足: goodsId={}, 当前库存={}", threadName, goodsId, beforeStock);
            return Result.fail("库存不足");
        }

        int afterStock = beforeStock - 1;
        stock.setStock(afterStock);
        stockMapper.updateById(stock);

        log.info("[{}] 扣减成功: goodsId={}, {} -> {} [synchronized]", threadName, goodsId, beforeStock, afterStock);
        return Result.success("ok");
    }
}