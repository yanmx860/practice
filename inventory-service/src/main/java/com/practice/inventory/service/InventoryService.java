package com.practice.inventory.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.practice.common.result.PageResult;
import com.practice.common.result.RespBean;
import com.practice.inventory.mapper.ProductStockMapper;
import com.practice.inventory.model.ProductStock;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存服务
 * @author ymx
 * @since 2026-02-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductStockMapper productStockMapper;

    // 原子扣减库存：使用 setSql 将条件检查与更新合并为一条 SQL，消除并发间隙
    // UPDATE t_product_stock SET available_stock = available_stock - #{quantity},
    //   frozen_stock = frozen_stock + #{quantity}, version = version + 1
    // WHERE product_id = #{productId} AND available_stock >= #{quantity}
    // 数据库行锁 + WHERE 条件保证不会超卖
    // @GlobalTransactional: Seata 全局事务
    //   - Feign 路径（OrderService 调用）：XID 从 OrderService 传播过来，作为全局事务的一个分支
    //   - MQ 路径（StockDeductionConsumer 调用）：独立开启全局事务（单分支），演示 Seata AT 模式
    // 注意：事务隔离级别为读已提交，原子 SQL 先于全局锁生效，可防止并发超卖
    @GlobalTransactional(name = "inventory-deduct", rollbackFor = Exception.class)
    public RespBean deductStock(Long orderId, Long productId, Integer quantity) {
        log.info("deductStock orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        // 原子 SQL：条件（库存足够）和更新（扣减+冻结）在同一条语句完成，无并发间隙
        LambdaUpdateWrapper<ProductStock> uw = new LambdaUpdateWrapper<ProductStock>()
                .eq(ProductStock::getProductId, productId)
                .ge(ProductStock::getAvailableStock, quantity)
                .setSql("available_stock = available_stock - " + quantity)
                .setSql("frozen_stock = frozen_stock + " + quantity)
                .setSql("version = version + 1");
        int rows = productStockMapper.update(null, uw);
        if (rows == 0) {
            // 两种可能：productId 不存在，或库存不足
            boolean exists = productStockMapper.selectCount(
                    new LambdaUpdateWrapper<ProductStock>().eq(ProductStock::getProductId, productId)) > 0;
            if (!exists) {
                log.error("product stock not found, productId={}", productId);
                return RespBean.error("Product stock not found");
            }
            log.error("insufficient stock, productId={}, required={}", productId, quantity);
            return RespBean.error("Insufficient stock");
        }
        log.info("stock deducted successfully, orderId={}", orderId);
        return RespBean.ok("Stock deducted successfully");
    }

    // 原子释放冻结库存（回滚）：WHERE 条件防止 frozenStock 变负数
    @Transactional(rollbackFor = Exception.class)
    public RespBean releaseStock(Long orderId, Long productId, Integer quantity) {
        log.info("releaseStock orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        LambdaUpdateWrapper<ProductStock> uw = new LambdaUpdateWrapper<ProductStock>()
                .eq(ProductStock::getProductId, productId)
                .ge(ProductStock::getFrozenStock, quantity)
                .setSql("frozen_stock = frozen_stock - " + quantity)
                .setSql("available_stock = available_stock + " + quantity)
                .setSql("version = version + 1");
        int rows = productStockMapper.update(null, uw);
        if (rows == 0) {
            log.error("releaseStock failed, productId={}, quantity={}", productId, quantity);
            return RespBean.error("Release stock failed");
        }
        log.info("stock released successfully, orderId={}", orderId);
        return RespBean.ok("Stock released successfully");
    }

    // 原子确认扣减：仅减少 frozenStock，WHERE 条件防止 frozenStock 变负数
    @Transactional(rollbackFor = Exception.class)
    public RespBean confirmStock(Long orderId, Long productId, Integer quantity) {
        log.info("confirmStock orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        LambdaUpdateWrapper<ProductStock> uw = new LambdaUpdateWrapper<ProductStock>()
                .eq(ProductStock::getProductId, productId)
                .ge(ProductStock::getFrozenStock, quantity)
                .setSql("frozen_stock = frozen_stock - " + quantity)
                .setSql("version = version + 1");
        int rows = productStockMapper.update(null, uw);
        if (rows == 0) {
            log.error("confirmStock failed, productId={}, quantity={}", productId, quantity);
            return RespBean.error("Confirm stock failed");
        }
        log.info("stock confirmed successfully, orderId={}", orderId);
        return RespBean.ok("Stock confirmed successfully");
    }
    public PageResult listProducts(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<ProductStock> list = productStockMapper.selectList(null);
        PageInfo<ProductStock> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), page, pageSize, pageInfo.getList());
    }
}
