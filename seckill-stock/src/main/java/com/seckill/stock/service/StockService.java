package com.seckill.stock.service;

import com.seckill.common.entity.GoodsStock;
import com.seckill.common.result.Result;
import com.seckill.stock.config.StockMqConfig;
import com.seckill.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ReentrantLock lock = new ReentrantLock();

    private static final Duration REDIS_LOCK_EXPIRE = Duration.ofSeconds(10);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """,
            Long.class
    );
    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>(
            """
            local stock = redis.call('get', KEYS[1])
            if not stock then
                return -1
            end
            if tonumber(stock) <= 0 then
                return -2
            end
            return redis.call('decr', KEYS[1])
            """,
            Long.class
    );

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

    /**
     * V3-1: 数据库原子扣减。
     * 原理：把库存判断和库存扣减合并成一条 UPDATE，由数据库行锁保证并发安全。
     * 优点：实现简单、可靠性高，不依赖额外中间件，适合中等并发和强一致优先的业务。
     * 缺点：高并发会把压力集中到数据库，热点商品会形成热点行。
     * 适用场景：库存量不大、并发可控、希望先用数据库能力兜住正确性的场景。
     */
    public Result<String> decreaseV31(Long goodsId) {
        int rows = stockMapper.decreaseAtomic(goodsId);
        if (rows <= 0) {
            log.warn("[DB-Atomic] 扣减失败，库存不足或商品不存在: goodsId={}", goodsId);
            return Result.fail("库存不足");
        }

        log.info("[DB-Atomic] 扣减成功: goodsId={}", goodsId);
        return Result.success("ok");
    }

    /**
     * V3-2: 数据库乐观锁。
     * 原理：先读取库存和 version，更新时带上 version 条件，只有读到同一版本的线程能更新成功。
     * 优点：不阻塞等待，冲突较少时性能好，能显式观察并发冲突。
     * 缺点：热点高冲突场景失败率高，若盲目重试会放大数据库压力。
     * 适用场景：并发冲突不极端、允许失败重试或快速失败的业务。
     */
    public Result<String> decreaseV32(Long goodsId) {
        GoodsStock stock = stockMapper.selectById(goodsId);
        if (stock == null) {
            log.warn("[DB-Optimistic] 商品不存在: goodsId={}", goodsId);
            return Result.fail("商品不存在");
        }

        if (stock.getStock() <= 0) {
            log.warn("[DB-Optimistic] 库存不足: goodsId={}, 当前库存={}", goodsId, stock.getStock());
            return Result.fail("库存不足");
        }

        int rows = stockMapper.decreaseOptimistic(goodsId, stock.getVersion());
        if (rows <= 0) {
            log.warn("[DB-Optimistic] 版本冲突，扣减失败: goodsId={}, version={}", goodsId, stock.getVersion());
            return Result.fail("并发冲突，请重试");
        }

        log.info("[DB-Optimistic] 扣减成功: goodsId={}, version={}", goodsId, stock.getVersion());
        return Result.success("ok");
    }

    /**
     * V3-3: Redis 分布式锁。
     * 原理：所有库存服务实例竞争同一个 Redis 锁 key，拿到锁后再执行数据库读、判断、扣减。
     * 优点：能跨 JVM/跨实例保护临界区，适合从本地锁演进到分布式锁的学习和中等并发场景。
     * 缺点：业务执行时间超过锁过期时间可能带来并发风险；手写锁要处理唯一值删除、过期时间、异常释放。
     * 适用场景：需要保护一段不能改成单 SQL 的临界区，且 Redis 可用性满足业务要求。
     */
    public Result<String> decreaseV33(Long goodsId) {
        String lockKey = "lock:stock:" + goodsId;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, REDIS_LOCK_EXPIRE);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("[Redis-Lock] 获取分布式锁失败: goodsId={}", goodsId);
            return Result.fail("系统繁忙，请稍后重试");
        }

        try {
            GoodsStock stock = stockMapper.selectById(goodsId);
            if (stock == null) {
                log.warn("[Redis-Lock] 商品不存在: goodsId={}", goodsId);
                return Result.fail("商品不存在");
            }

            int beforeStock = stock.getStock();
            if (beforeStock <= 0) {
                log.warn("[Redis-Lock] 库存不足: goodsId={}, 当前库存={}", goodsId, beforeStock);
                return Result.fail("库存不足");
            }

            stock.setStock(beforeStock - 1);
            stockMapper.updateById(stock);
            log.info("[Redis-Lock] 扣减成功: goodsId={}, {} -> {}", goodsId, beforeStock, beforeStock - 1);
            return Result.success("ok");
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), lockValue);
        }
    }

    /**
     * V3-4: MQ 串行化。
     * 原理：接口只负责把扣库存请求写入 RabbitMQ，消费者从队列中逐条处理，用队列削峰并把热点写操作串行化。
     * 优点：削峰能力强，能保护数据库，适合秒杀这种突发流量场景。
     * 缺点：调用变成异步，接口只能返回“排队中”，后续要继续补订单状态查询、失败补偿、幂等消费。
     * 适用场景：流量峰值高、允许异步确认结果、需要保护下游数据库的场景。
     */
    public Result<String> decreaseV34(Long goodsId) {
        rabbitTemplate.convertAndSend(StockMqConfig.EXCHANGE, StockMqConfig.ROUTING_KEY, String.valueOf(goodsId));
        log.info("[MQ-Serial] 扣库存请求已入队: goodsId={}", goodsId);
        return Result.success("排队中");
    }

    /**
     * V3-5: Redis 预扣库存。
     * 原理：首次从数据库懒加载库存到 Redis，之后用 Lua 在 Redis 中原子判断并扣减，Redis 成功后再落库。
     * 优点：性能高、抗并发能力强，能把热点库存判断前置到 Redis。
     * 缺点：需要处理 Redis 和数据库一致性、失败补偿、重复请求、缓存初始化等复杂问题。
     * 适用场景：高并发秒杀、热点商品明显、可以接受更复杂一致性治理的场景。
     */
    public Result<String> decreaseV35(Long goodsId) {
        String stockKey = "stock:goods:" + goodsId;
        initRedisStockIfNecessary(goodsId, stockKey);

        Long remain = stringRedisTemplate.execute(PRE_DEDUCT_SCRIPT, Collections.singletonList(stockKey));
        if (remain == null || remain == -1) {
            log.warn("[Redis-PreDeduct] Redis 库存未初始化或扣减异常: goodsId={}", goodsId);
            return Result.fail("库存初始化失败");
        }

        if (remain == -2) {
            log.warn("[Redis-PreDeduct] Redis 预扣库存失败，库存不足: goodsId={}", goodsId);
            return Result.fail("库存不足");
        }

        int rows = stockMapper.decreaseAtomic(goodsId);
        if (rows <= 0) {
            stringRedisTemplate.opsForValue().increment(stockKey);
            log.warn("[Redis-PreDeduct] 数据库扣减失败，已补偿 Redis 库存: goodsId={}", goodsId);
            return Result.fail("库存不足");
        }

        log.info("[Redis-PreDeduct] 扣减成功: goodsId={}, Redis剩余库存={}", goodsId, remain);
        return Result.success("ok");
    }

    private void initRedisStockIfNecessary(Long goodsId, String stockKey) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
            return;
        }

        GoodsStock stock = stockMapper.selectById(goodsId);
        if (stock == null) {
            return;
        }

        stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(stock.getStock()));
    }
}
