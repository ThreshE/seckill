package com.seckill.stock.service;

import com.seckill.common.entity.GoodsStock;
import com.seckill.common.result.Result;
import com.seckill.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final ReentrantLock lock = new ReentrantLock();

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

    /**
     * V2: 使用 ReentrantLock 保证库存扣减的原子性
     * ReentrantLock 基于 AQS 实现，便于学习显式加锁和释放锁的过程
     * 局限：仅单机有效，多实例部署时仍会超卖
     * 当下没有使用tryLock，因为要和synchronized做对比，实际的秒杀场景中是需要使用tryLock快速失败，减少线程排队等待
     */
    public Result<String> decreaseV2(Long goodsId) {
        String threadName = Thread.currentThread().getName();

        lock.lock();
        try {
            GoodsStock stock = stockMapper.selectById(goodsId);
            if (stock == null) {
                log.warn("[{}] 商品不存在 goodsId={} [ReentrantLock]", threadName, goodsId);
                return Result.fail("商品不存在");
            }

            int beforeStock = stock.getStock();
            log.info("[{}] 读库存: goodsId={}, 当前库存={} [ReentrantLock]", threadName, goodsId, beforeStock);

            if (beforeStock <= 0) {
                log.warn("[{}] 库存不足: goodsId={}, 当前库存={} [ReentrantLock]", threadName, goodsId, beforeStock);
                return Result.fail("库存不足");
            }

            int afterStock = beforeStock - 1;
            stock.setStock(afterStock);
            stockMapper.updateById(stock);

            log.info("[{}] 扣减成功: goodsId={}, {} -> {} [ReentrantLock]", threadName, goodsId, beforeStock, afterStock);
            return Result.success("ok");
        } finally {
            lock.unlock();
        }
    }
}
