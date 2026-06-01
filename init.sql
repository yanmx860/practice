-- ============================================================
-- 微服务商城 数据库初始化脚本
-- 3 个 MySQL 库，对应 3 个业务服务
-- 每个库附带 Seata AT 模式的 undo_log 表
-- ============================================================

-- ===================== 1. auth-service =====================
CREATE DATABASE IF NOT EXISTS `practice_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `practice_auth`;

-- 系统用户表
CREATE TABLE `sys_user` (
  `id`          INT           NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)   NOT NULL COMMENT '用户名',
  `password`    VARCHAR(255)  NOT NULL COMMENT '密码(BCrypt)',
  `permissions` VARCHAR(500)  DEFAULT NULL COMMENT '权限列表(逗号分隔，如 order:export,order:view)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- Seata AT 分支事务日志
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)    NOT NULL,
  `xid`           VARCHAR(128)  NOT NULL,
  `context`       VARCHAR(128)  NOT NULL,
  `rollback_info` LONGBLOB      NOT NULL,
  `log_status`    INT(11)       NOT NULL,
  `log_created`   DATETIME      NOT NULL,
  `log_modified`  DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unionkey` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

-- 初始测试用户（密码: 123456，BCrypt 预生成）
INSERT INTO `sys_user` (`username`, `password`, `permissions`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'order:export,order:view,user:manage'),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'order:view');

-- ===================== 2. order-service + export-service =====================
CREATE DATABASE IF NOT EXISTS `practice_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `practice_order`;

-- 订单表
CREATE TABLE `t_order` (
  `id`           BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`     VARCHAR(50)   DEFAULT NULL COMMENT '订单编号',
  `user_id`      INT           DEFAULT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '订单金额',
  `status`       INT           DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已完成 3-失败 4-已取消',
  `channel`      VARCHAR(20)   DEFAULT NULL COMMENT '支付渠道: alipay/wechat/card',
  `payment_id`   INT           DEFAULT NULL COMMENT '支付记录ID',
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      INT           DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 订单项表
CREATE TABLE `t_order_item` (
  `id`           BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '项ID',
  `order_id`     BIGINT(20)    NOT NULL COMMENT '订单ID',
  `product_id`   BIGINT(20)    DEFAULT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100)  DEFAULT NULL COMMENT '商品名称',
  `quantity`     INT           DEFAULT NULL COMMENT '数量',
  `price`        DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项';

-- 订单操作日志表
CREATE TABLE `t_order_log` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id`    BIGINT(20)   NOT NULL COMMENT '订单ID',
  `action`      VARCHAR(50)  DEFAULT NULL COMMENT '操作动作',
  `detail`      VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志';

-- Seata AT 分支事务日志
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)    NOT NULL,
  `xid`           VARCHAR(128)  NOT NULL,
  `context`       VARCHAR(128)  NOT NULL,
  `rollback_info` LONGBLOB      NOT NULL,
  `log_status`    INT(11)       NOT NULL,
  `log_created`   DATETIME      NOT NULL,
  `log_modified`  DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unionkey` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

-- 初始化示例订单（500万测试数据用导出做压力测试）
INSERT INTO `t_order` (`order_no`, `user_id`, `total_amount`, `status`, `channel`) VALUES
('ORD202601010001', 1, 199.00, 2, 'alipay'),
('ORD202601010002', 1, 299.00, 0, 'wechat'),
('ORD202601010003', 2, 59.90, 2, 'card');

INSERT INTO `t_order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES
(1, 1001, 'Java 编程思想', 1, 199.00),
(2, 1002, 'Spring 实战', 1, 299.00),
(3, 1003, '鼠标垫', 2, 29.95);

-- ===================== 3. inventory-service =====================
CREATE DATABASE IF NOT EXISTS `practice_inventory` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `practice_inventory`;

-- 商品库存表
CREATE TABLE `t_product_stock` (
  `id`              BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_id`      BIGINT(20)  NOT NULL COMMENT '商品ID',
  `product_name`    VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
  `total_stock`     INT          DEFAULT 0 COMMENT '总库存',
  `frozen_stock`    INT          DEFAULT 0 COMMENT '冻结库存',
  `available_stock` INT          DEFAULT 0 COMMENT '可用库存',
  `version`         INT          DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存';

-- Seata AT 分支事务日志
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)    NOT NULL,
  `xid`           VARCHAR(128)  NOT NULL,
  `context`       VARCHAR(128)  NOT NULL,
  `rollback_info` LONGBLOB      NOT NULL,
  `log_status`    INT(11)       NOT NULL,
  `log_created`   DATETIME      NOT NULL,
  `log_modified`  DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unionkey` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

-- 初始化商品库存
INSERT INTO `t_product_stock` (`product_id`, `product_name`, `total_stock`, `available_stock`, `frozen_stock`, `version`) VALUES
(1001, 'Java 编程思想',      500,  500, 0, 0),
(1002, 'Spring 实战',       300,  300, 0, 0),
(1003, '鼠标垫',           1000, 1000, 0, 0),
(1004, '机械键盘',          200,  200, 0, 0),
(1005, '4K 显示器',          50,   50, 0, 0);
