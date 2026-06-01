package com.practice.inventory.controller;

import com.practice.common.result.PageResult;
import com.practice.common.result.RespBean;
import com.practice.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** * 库存控制器 * @author ymx * @since 2026-02-04 */
@Tag(name = "库存管理", description = "商品库存扣减、释放、查询")
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // POST /api/inventory/deduct ：扣减库存端点
    // 接收 orderId / productId / quantity，调用 service 层执行 Seata 分布式事务扣减
    @Operation(summary = "扣减库存", description = "扣减可用库存，增加冻结库存（Seata分布式事务）")
    @PostMapping("/deduct")
    public RespBean deduct(@Parameter(description = "订单ID") @RequestParam Long orderId,
                           @RequestParam Long productId,
                           @RequestParam Integer quantity) {
        log.info("deduct request orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        return inventoryService.deductStock(orderId, productId, quantity);
    }

    // POST /api/inventory/release ：释放库存端点
    // 将冻结库存释放回可用库存，用于 Seata 二阶段回滚或订单取消
    @Operation(summary = "释放库存", description = "释放冻结库存回可用库存（事务回滚时调用）")
    @PostMapping("/release")
    public RespBean release(@Parameter(description = "订单ID") @RequestParam Long orderId,
                            @RequestParam Long productId,
                            @RequestParam Integer quantity) {
        log.info("release request orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        return inventoryService.releaseStock(orderId, productId, quantity);
    }

    // GET /api/inventory/list ：分页查询商品库存列表端点
    @Operation(summary = "商品列表", description = "分页查询所有商品库存信息")
    @GetMapping("/list")
    public RespBean list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int pageSize) {
        PageResult result = inventoryService.listProducts(page, pageSize);
        return RespBean.ok("ok", result);
    }
}
