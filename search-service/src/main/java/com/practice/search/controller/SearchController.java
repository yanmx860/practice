package com.practice.search.controller;

import com.practice.common.result.RespBean;
import com.practice.search.document.ProductDocument;
import com.practice.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** * 搜索控制器 * @author ymx * @since 2026-02-24 */
@Tag(name = "商品搜索", description = "ES全文搜索、索引同步")

@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchController {

    @Autowired
    private SearchService searchService;

    // GET /api/search/search ：全文搜索端点
    // keyword：用户输入的关键词；page：页码（默认0）；size：每页条数（默认10）
    @Operation(summary = "搜索商品", description = "基于ES的全文检索，支持ik中文分词")
    @GetMapping("/search")
    public RespBean search(@Parameter(description = "搜索关键词") @RequestParam String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        List<ProductDocument> result = searchService.searchProduct(keyword, page, size);
        return RespBean.ok(result);
    }

    // POST /api/search/syncProduct ：手动同步商品到 ES 索引
    // productId：需要同步的商品 ID（构建 ProductDocument 后写入 ES）
    @Operation(summary = "同步商品", description = "将商品信息同步到ES索引")
    @PostMapping("/syncProduct")
    public RespBean syncProduct(@Parameter(description = "商品ID") @RequestParam Long productId) {
        ProductDocument product = new ProductDocument();
        product.setId(productId);
        product.setName("Product-" + productId);
        ProductDocument saved = searchService.syncProduct(product);
        return RespBean.ok(saved);
    }

    // DELETE /api/search/{id} ：从 ES 中删除指定商品索引
    // id：商品 ID，对应 ES 文档 _id
    @Operation(summary = "删除商品索引", description = "从ES中删除指定商品")
    @DeleteMapping("/{id}")
    public RespBean delete(@Parameter(description = "商品ID") @PathVariable Long id) {
        searchService.deleteProduct(id);
        return RespBean.ok();
    }
}
