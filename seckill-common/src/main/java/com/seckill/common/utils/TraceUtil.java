package com.seckill.common.utils;

import java.util.UUID;

public class TraceUtil {
    public static String getTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}