-- ============================================================
-- Seata 分布式事务表 (database: seata)
-- ============================================================
CREATE DATABASE IF NOT EXISTS seata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata;

-- 全局事务表
CREATE TABLE IF NOT EXISTS global_table (
    xid                       VARCHAR(128) NOT NULL,
    transaction_id            BIGINT,
    status                    TINYINT      NOT NULL,
    application_id            VARCHAR(32),
    transaction_service_group VARCHAR(32),
    transaction_name          VARCHAR(128),
    timeout                   INT,
    begin_time                BIGINT,
    application_data          VARCHAR(2000),
    gmt_create                DATETIME,
    gmt_modified              DATETIME,
    PRIMARY KEY (xid),
    KEY idx_status (status),
    KEY idx_transaction_id (transaction_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 分支事务表
CREATE TABLE IF NOT EXISTS branch_table (
    branch_id         BIGINT       NOT NULL,
    xid               VARCHAR(128) NOT NULL,
    transaction_id    BIGINT,
    resource_group_id VARCHAR(32),
    resource_id       VARCHAR(256),
    branch_type       VARCHAR(8),
    status            TINYINT,
    client_id         VARCHAR(64),
    application_data  VARCHAR(2000),
    gmt_create        DATETIME(6),
    gmt_modified      DATETIME(6),
    PRIMARY KEY (branch_id),
    KEY idx_xid (xid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 全局锁表
CREATE TABLE IF NOT EXISTS lock_table (
    row_key        VARCHAR(128) NOT NULL,
    xid            VARCHAR(128),
    transaction_id BIGINT,
    branch_id      BIGINT       NOT NULL,
    resource_id    VARCHAR(256),
    table_name     VARCHAR(32),
    pk             VARCHAR(36),
    status         TINYINT      NOT NULL DEFAULT '0',
    gmt_create     DATETIME,
    gmt_modified   DATETIME,
    PRIMARY KEY (row_key),
    KEY idx_branch_id (branch_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 分布式锁表 (Seata 2.x 新增)
CREATE TABLE IF NOT EXISTS distributed_lock_table (
    lock_key     VARCHAR(128) NOT NULL,
    lock_value   VARCHAR(255) DEFAULT NULL,
    expire       BIGINT       DEFAULT NULL,
    gmt_create   DATETIME     DEFAULT NULL,
    gmt_modified DATETIME     DEFAULT NULL,
    PRIMARY KEY (lock_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- 秒杀业务表 (database: seckill)
-- ============================================================
CREATE DATABASE IF NOT EXISTS seckill CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seckill;

-- 商品库存表
CREATE TABLE IF NOT EXISTS goods_stock (
    id          BIGINT         PRIMARY KEY,
    goods_name  VARCHAR(100)   NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    price       DECIMAL(10, 2) NOT NULL,
    version     INT            NOT NULL DEFAULT 0,
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 初始化库存: iPhone 17 Pro, 1000 件, 7999 元
INSERT INTO goods_stock (id, goods_name, stock, price)
VALUES (1, 'iPhone 17 Pro', 1000, 7999.00)
ON DUPLICATE KEY UPDATE goods_name = VALUES(goods_name), stock = VALUES(stock);

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS seckill_order (
    id          BIGINT         PRIMARY KEY,
    order_no    VARCHAR(64)    NOT NULL,
    user_id     BIGINT         NOT NULL,
    goods_id    BIGINT         NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用户账户表（模拟支付）
CREATE TABLE IF NOT EXISTS user_account (
    id          BIGINT         PRIMARY KEY,
    user_id     BIGINT         NOT NULL UNIQUE,
    balance     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 初始化用户: user_id=10001, 余额 100000
INSERT INTO user_account (id, user_id, balance)
VALUES (1, 10001, 100000.00)
ON DUPLICATE KEY UPDATE balance = VALUES(balance);

-- 并发执行日志表（用于学习观察）
CREATE TABLE IF NOT EXISTS concurrency_log (
    id           BIGINT         AUTO_INCREMENT PRIMARY KEY,
    trace_id     VARCHAR(64)    NOT NULL,
    service_name VARCHAR(50)    DEFAULT NULL,
    thread_name  VARCHAR(100)   DEFAULT NULL,
    step         VARCHAR(100)   DEFAULT NULL,
    before_value VARCHAR(100)   DEFAULT NULL,
    after_value  VARCHAR(100)   DEFAULT NULL,
    lock_type    VARCHAR(50)    DEFAULT NULL,
    cost_time    BIGINT         DEFAULT NULL,
    create_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_trace_id (trace_id),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
