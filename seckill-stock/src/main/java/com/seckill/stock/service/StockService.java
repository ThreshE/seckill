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
     * V0: 无锁直扣 MySQL — 故意不处理并发问题
     */
    public Result<String> decrease(Long goodsId) {
        GoodsStock stock = stockMapper.selectById(goodsId);
        if (stock == null) {
            return Result.fail("商品不存在");
        }
        if (stock.getStock() <= 0) {
            return Result.fail("库存不足");
        }
        // 非原子操作：读 → 改 → 写（并发下超卖）
        stock.setStock(stock.getStock() - 1);
        stockMapper.updateById(stock);
        return Result.success("ok");
    }
}