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

    public Result<String> decrease(Long goodsId) {
        String threadName = Thread.currentThread().getName();

        // 1. 读取库存（非原子操作的第一步）
        GoodsStock stock = stockMapper.selectById(goodsId);
        if (stock == null) {
            log.warn("[{}] 商品不存在 goodsId={}", threadName, goodsId);
            return Result.fail("商品不存在");
        }

        int beforeStock = stock.getStock();
        log.info("[{}] 读库存: goodsId={}, 当前库存={}", threadName, goodsId, beforeStock);

        // 2. 检查库存
        if (beforeStock <= 0) {
            log.warn("[{}] 库存不足: goodsId={}, 当前库存={}", threadName, goodsId, beforeStock);
            return Result.fail("库存不足");
        }

        // 3. 修改库存（非原子操作的第二步）
        int afterStock = beforeStock - 1;
        stock.setStock(afterStock);

        // 4. 写回数据库（非原子操作的第三步）
        //    并发问题：多个线程同时读到 beforeStock=20，都写入 19
        //    最终库存不是 20-25=-5，而是接近 19 或 18（最后一次写入覆盖前面的）
        stockMapper.updateById(stock);

        log.info("[{}] 写库存: goodsId={}, {} -> {}  <== 并发陷阱：其他线程可能也看到 {} 并写回了 {}",
                threadName, goodsId, beforeStock, afterStock, beforeStock, afterStock);

        return Result.success("ok");
    }
}