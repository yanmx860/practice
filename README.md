# Practice 综合练习项目

全栈微服务练习项目，集成主流中间件与框架，涵盖认证、订单、库存、搜索、导出、定时任务等业务场景。

## 技术栈

### 框架与版本
| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Cloud | 2021.0.8 | 微服务治理 |
| Spring Cloud Alibaba | 2021.0.6.0 | Nacos/Sentinel/Seata 集成 |
| Spring Security | 5.7.x | 认证授权 |
| MyBatis-Plus | 3.5.3 | ORM 框架 |
| PageHelper | 5.3.3 | 分页插件 |
| SpringDoc OpenAPI | 1.7.0 | 接口文档（Swagger UI） |

### 中间件
| 技术 | 用途 |
|---|---|
| **Nacos** | 配置中心 + 服务注册发现 |
| **Sentinel** | 流量控制、熔断降级 |
| **Seata AT** | 分布式事务（订单→库存） |
| **RabbitMQ** | 异步消息（订单事件→库存扣减） |
| **Kafka** | 数据同步（→ES 搜索引擎） |
| **ElasticSearch** | 商品全文检索 |
| **XXL-Job** | 分布式定时任务（日报/统计） |
| **Redis** | （预留，当前未启用） |

### 数据导出
| 技术 | 用途 |
|---|---|
| **EasyExcel** | 百万级数据流式导出 |
| **Semaphore** | 控制并发查询数（背压） |
| **线程池** | 多线程分页并行查询 |
| **synchronized** | ExcelWriter 线程安全写入 |

### 设计模式
| 模式 | 实现 |
|---|---|
| **模板方法** | `AbstractOrderProcessor` 定义订单处理骨架 → `NormalOrderProcessor` 实现具体步骤 |
| **策略模式** | `PaymentStrategy` 接口 → Alipay / Wechat / Card 策略 → `StrategyRegistry` 注册管理 |
| **ThreadLocal** | `UserContextHolder` 在线程内传递用户上下文（userId / username / permissions） |
| **生产者-消费者** | 数据导出：线程池分页查询（生产者）→ Semaphore 背压 → 同步写入 Excel（消费者） |
| **自定义注解 + AOP** | `@RequirePermission` 注解 + `PermissionAspect` 切面实现权限校验 |

---

## 模块说明

### common —— 公共模块

```
common/src/main/java/com/practice/common/
├── annotation/
│   ├── RequirePermission.java      # 自定义权限注解
│   └── PermissionAspect.java       # AOP 切面：校验当前用户是否有指定权限
├── thread/
│   ├── ThreadPoolConfig.java       # 线程池配置：commonExecutor(10/20)、exportExecutor(5/10)
│   └── UserContextHolder.java      # ThreadLocal 工具：存放 userId / username / permissions
├── pattern/
│   ├── template/
│   │   └── AbstractOrderProcessor.java   # 模板方法：定义订单处理流程骨架
│   └── strategy/
│       ├── PaymentStrategy.java          # 策略接口
│       ├── StrategyRegistry.java         # 策略注册器（PostConstruct 自动扫描）
│       └── impl/
│           ├── AlipayStrategy.java       # 支付宝支付
│           ├── WechatPayStrategy.java    # 微信支付
│           └── CardPayStrategy.java      # 银行卡支付
├── result/
│   ├── RespBean.java              # 统一响应体 {status, msg, obj}
│   └── PageResult.java            # 分页结果 {total, page, pageSize, records}
├── enums/
│   └── OrderStatusEnum.java       # 订单状态枚举
├── exception/
│   ├── BizException.java          # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理
├── util/
│   └── JwtUtil.java               # JWT 工具（accessToken + refreshToken）
└── model/
    └── BaseEntity.java            # MyBatis-Plus 基类（id / createTime / updateTime / deleted）
```

其他服务引用 common 模块：
```xml
<dependency>
    <groupId>com.practice</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

### auth-service（8001）—— 认证服务

**技术**：Nacos + Sentinel + Spring Security + JWT + MyBatis-Plus

| 接口 | 说明 |
|---|---|
| `POST /api/auth/login` | 用户名密码登录，返回 accessToken + refreshToken |
| `POST /api/auth/refresh` | 使用 refreshToken 换取新的 accessToken |

**功能点**：
- 登录失败 5 次锁定账户 30 分钟（内存 Map）
- accessToken 有效期 30 分钟，refreshToken 有效期 30 天
- BCrypt 加密存储密码
- JWT 包含 userId / username / permissions

**Swagger**：`http://localhost:8001/swagger-ui/index.html`

---

### order-service（8002）—— 订单服务

**技术**：Nacos + Sentinel + Feign + Seata AT + RabbitMQ + ThreadLocal + 设计模式 + 线程池

| 接口 | 说明 |
|---|---|
| `POST /api/orders/process/{orderId}` | 处理单个订单（模板方法 + 策略模式） |
| `POST /api/orders/processBatch` | 批量处理订单（多线程 + ThreadLocal） |

**业务流程图**：
```
OrderController
    ↓
OrderService.processOrder()
    ├── @GlobalTransactional（Seata AT 分布式事务）
    ├── ① 更新订单状态 → 处理中
    ├── ② Feign → InventoryService.deductStock()（扣库存）
    ├── ③ StrategyRegistry → PaymentStrategy.pay()（策略模式支付）
    ├── ④ 更新订单状态 → 已完成
    ├── ⑤ RabbitMQ → 发送 order.created 事件
    └── ⑥ @Async → Feign → SearchService.syncProduct()（同步 ES）
```

**ParallelOrderService**：
- 使用 `CompletableFuture.runAsync()` + `commonExecutor` 线程池
- `CountDownLatch` 等待所有订单处理完成
- `UserContextHolder` 在每个线程中 set/clear ThreadLocal 上下文

**NormalOrderProcessor**：
- 继承 `AbstractOrderProcessor`，实现模板方法的各个步骤

**Swagger**：`http://localhost:8002/swagger-ui/index.html`

---

### inventory-service（8003）—— 库存服务

**技术**：Nacos + Seata AT + RabbitMQ + MyBatis-Plus + PageHelper

| 接口 | 说明 |
|---|---|
| `POST /api/inventory/deduct` | 扣减库存（Seata 分布式事务） |
| `POST /api/inventory/release` | 释放冻结库存（事务回滚） |
| `GET /api/inventory/list` | 分页查询商品库存（PageHelper） |

**消息消费**：
- 监听 `stock.deduct.queue`（绑定 `order.exchange` + `order.created` routing key）
- 异步消费订单事件，调用 `deductStock()` 扣减库存

**数据模型**：`t_product_stock` 表含乐观锁 `@Version`

**Swagger**：`http://localhost:8003/swagger-ui/index.html`

---

### search-service（8004）—— 搜索服务

**技术**：Nacos + ElasticSearch + Kafka + Spring Data Elasticsearch

| 接口 | 说明 |
|---|---|
| `GET /api/search/search` | ES 全文检索（ik 中文分词） |
| `POST /api/search/syncProduct` | 手动同步商品到 ES 索引 |
| `DELETE /api/search/{id}` | 从 ES 删除商品索引 |

**消息消费**：
- 监听 `product-sync-topic`（Kafka Topic）
- `ProductSyncConsumer` 消费消息后调用 `SearchService.syncProduct()`
- 支持 JSON 序列化/反序列化

**ES 查询**：`ElasticsearchRestTemplate` + `NativeSearchQueryBuilder` + bool query（name / description 匹配）

**Swagger**：`http://localhost:8004/swagger-ui/index.html`

---

### export-service（8005）—— 导出服务

**技术**：Nacos + EasyExcel + PageHelper + 线程池 + Semaphore + CountDownLatch

| 接口 | 说明 |
|---|---|
| `GET /api/export/orders` | 百万级订单导出（带日期筛选） |

**防 OOM 架构**：
```
主线程                             导出线程池 (exportExecutor)
  │                                       │
  ├─ for 循环提交任务 ──────────────────→  ├─ Semaphore 限流（最多 3 并发）
  │                                       │
  │                                       ├─ PageHelper.startPage(pageNum, 5000)
  │                                       ├─ mapper.selectList()  ← 仅 5000 条/次
  │                                       │
  │                                       ├─ synchronized 写 Excel
  │                                       │   └─ excelWriter.write(records, sheet)
  │                                       │      └─ inMemory(false) → 写临时文件
  │                                       │
  │                                       └─ latch.countDown()
  │
  └─ latch.await() 等待全部完成
```

**内存安全验证**：
```
同时最多 3 个查询线程 → 3 × 5000 条 × ~400B ≈ 6MB
synchronized 写 Excel  → 同一时刻只有 1 份数据在写入
inMemory(false)        → EasyExcel 不缓存到 JVM 堆
总计 ≈ 10MB            → 远低于 OOM 阈值
```

**Swagger**：`http://localhost:8005/swagger-ui/index.html`

---

### report-service（8006）—— 报表服务

**技术**：Nacos + XXL-Job + EasyExcel + MyBatis-Plus

| 任务 | 说明 |
|---|---|
| `dailyReportJob` | 每日自动生成销售日报 Excel |
| `orderStatisticsJob` | 订单统计数据报表 |

**XXL-Job 配置**：
```yaml
xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: report-service
      port: 9999
```

**Swagger**：`http://localhost:8006/swagger-ui/index.html`

---

## 端口与数据库

| 服务 | 端口 | 数据库 |
|---|---|---|
| auth-service | 8001 | `practice_auth` |
| order-service | 8002 | `practice_order` |
| inventory-service | 8003 | `practice_inventory` |
| search-service | 8004 | （无需数据库） |
| export-service | 8005 | `practice_order` |
| report-service | 8006 | `practice_report` |

MySQL 密码统一：`mysqlroot`

---

## 依赖环境

| 组件 | 地址 |
|---|---|
| JDK | 8+ |
| MySQL | 8.0（需建表） |
| Nacos | localhost:8848（命名空间 `practice`） |
| RabbitMQ | localhost:5672 |
| Kafka | localhost:9092 |
| ElasticSearch | localhost:9200（需安装 ik 分词插件） |
| XXL-Job Admin | localhost:8080/xxl-job-admin |
| Seata Server | localhost:8091 |
| Maven | 3.6+（推荐配置阿里云镜像） |

---

## Maven 镜像配置

`~/.m2/settings.xml` 中添加：

```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 编译与运行

```bash
# 全量编译
mvn clean compile

# 打包
mvn clean package -DskipTests

# 运行（单服务）
java -jar auth-service/target/auth-service-1.0.0.jar

# 运行（指定端口）
java -jar order-service/target/order-service-1.0.0.jar --server.port=8002
```

---

## 快速启动顺序

1. **启动中间件**：MySQL → Nacos → RabbitMQ → Kafka → ES → XXL-Job Admin → Seata Server
2. **初始化数据库**：在各 MySQL 数据库中执行对应的建表语句
3. **启动服务**：按依赖顺序启动 auth-service → inventory-service → order-service → search-service → export-service → report-service
4. **验证**：访问各服务 Swagger UI 或调用 API

---

## 关键业务流程

### 下单到搜索全流程
```
客户端 → OrderController
  → OrderService.processOrder()
    → (Seata AT) 扣库存 ← InventoryService
    → (策略模式) 支付
    → (RabbitMQ) 发送订单事件
      → StockDeductionConsumer ← InventoryService（异步扣库存）
    → (Kafka) 发送商品同步事件
      → ProductSyncConsumer ← SearchService（同步到 ES）
  → 返回结果
```

### 百万数据导出流程
```
客户端 GET /api/export/orders
  → @RequirePermission("order:export") 权限校验
  → exportOrders()
    → PageHelper 分页 5000 条/页
    → Semaphore 控制 3 线程并发查 DB
    → synchronized 排队写入 EasyExcel（inMemory=false）
    → 响应流式下载
```
