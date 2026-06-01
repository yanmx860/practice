package com.practice.search.consumer;

import com.practice.search.document.ProductDocument;
import com.practice.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** * 商品同步消息消费者 * @author ymx * @since 2026-02-25 */
@Component
@Slf4j
public class ProductSyncConsumer {

    @Autowired
    private SearchService searchService;

    // @KafkaListener：消费 product-sync-topic 主题的消息，consumer-group = search-group
    // 当商品信息变更时（新增/修改），生产者发送 ProductDocument 到 topic
    // consume() 调用 SearchService.syncProduct() —> productRepository.save() 实现 ES 文档写入/更新
    // 如需删除，生产者可发送带删除标记的消息，或单独调用 deleteProduct() 删除 ES 文档
    @KafkaListener(topics = "product-sync-topic", groupId = "search-group")
    public void consume(ProductDocument product) {
        log.info("Received product sync message: {}", product);
        searchService.syncProduct(product);
    }
}
