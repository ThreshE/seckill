package com.seckill.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createTime;
}