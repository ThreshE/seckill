package com.seckill.stock.mq;

import com.seckill.stock.config.StockMqConfig;
import com.seckill.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDecreaseConsumer {

    private final StockMapper stockMapper;

    @RabbitListener(queues = StockMqConfig.QUEUE)
    public void consume(String goodsIdMessage) {
        Long goodsId = Long.valueOf(goodsIdMessage);
        int rows = stockMapper.decreaseAtomic(goodsId);
        if (rows > 0) {
            log.info("[MQ-Serial] 串行消费扣减库存成功: goodsId={}", goodsId);
            return;
        }

        log.warn("[MQ-Serial] 串行消费扣减库存失败，可能库存不足: goodsId={}", goodsId);
    }
}
