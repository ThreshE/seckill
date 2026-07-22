package com.seckill.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_account")
public class UserAccount {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}