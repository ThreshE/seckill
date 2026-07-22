package com.seckill.payment.service;

import com.seckill.common.entity.UserAccount;
import com.seckill.common.result.Result;
import com.seckill.payment.mapper.AccountMapper;
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
     * V0: 无锁直扣余额 — 同样有并发问题
     */
    public Result<String> pay(Long userId, BigDecimal amount) {
        UserAccount account = accountMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUserId, userId)
        );
        if (account == null) {
            return Result.fail("账户不存在");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            return Result.fail("余额不足");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountMapper.updateById(account);
        return Result.success("ok");
    }
}