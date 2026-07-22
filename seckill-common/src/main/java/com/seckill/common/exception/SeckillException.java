package com.seckill.common.exception;

public class SeckillException extends RuntimeException {
    private Integer code;

    public SeckillException(String message) {
        super(message);
        this.code = 500;
    }

    public SeckillException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}