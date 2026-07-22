package com.seckill.payment.service;

import com.seckill.common.entity.UserAccount;
import com.seckill.common.result.Result;
import com.seckill.payment.mapper.AccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountMapper accountMapper;

    /**
     * V1: 使用 synchronized 保证余额扣减的原子性
     */
    public synchronized Result<String> pay(Long userId, BigDecimal amount) {
        String threadName = Thread.currentThread().getName();

        UserAccount account = accountMapper.selectOne(
            new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId)
        );
        if (account == null) {
            return Result.fail("账户不存在");
        }

        BigDecimal beforeBalance = account.getBalance();
        log.info("[{}] 读余额: userId={}, 当前余额={}", threadName, userId, beforeBalance);

        if (beforeBalance.compareTo(amount) < 0) {
            log.warn("[{}] 余额不足: userId={}, 余额={}, 需要={}", threadName, userId, beforeBalance, amount);
            return Result.fail("余额不足");
        }

        BigDecimal afterBalance = beforeBalance.subtract(amount);
        account.setBalance(afterBalance);
        accountMapper.updateById(account);

        log.info("[{}] 扣余额成功: userId={}, {} -> {} [synchronized]", threadName, userId, beforeBalance, afterBalance);
        return Result.success("ok");
    }
}