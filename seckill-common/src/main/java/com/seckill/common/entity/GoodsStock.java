package com.seckill.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods_stock")
public class GoodsStock {
    private Long id;
    private String goodsName;
    private Integer stock;
    private BigDecimal price;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}