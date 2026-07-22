package com.seckill.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.common.entity.GoodsStock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<GoodsStock> {

    @Update("UPDATE goods_stock SET stock = stock - 1 WHERE id = #{goodsId} AND stock > 0")
    int decreaseAtomic(@Param("goodsId") Long goodsId);

    @Update("UPDATE goods_stock SET stock = stock - 1, version = version + 1 WHERE id = #{goodsId} AND stock > 0 AND version = #{version}")
    int decreaseOptimistic(@Param("goodsId") Long goodsId, @Param("version") Integer version);
}
