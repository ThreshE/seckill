package com.seckill.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("concurrency_log")
public class ConcurrencyLog {
    private Long id;
    private String traceId;
    private String serviceName;
    private String threadName;
    private String step;
    private String beforeValue;
    private String afterValue;
    private String lockType;
    private Long costTime;
    private LocalDateTime createTime;
}