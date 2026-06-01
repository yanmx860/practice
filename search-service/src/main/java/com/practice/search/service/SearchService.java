package com.practice.search.service;

import com.practice.search.document.ProductDocument;
import com.practice.search.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** * 搜索服务 * @author ymx * @since 2026-02-23 */
@Service
@Slf4j
public class SearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    // ES bool query 组合查询：should 子句实现 OR 逻辑，匹配 name 或 description
    // matchQuery：对搜索内容做分词后再匹配（适合全文搜索）
    // keyword 查询（termQuery）：不分词、精确匹配，适合完全匹配场景（如 ID、状态码）
    // must 表示 AND，mustNot 表示排除，filter 表示过滤（不参与评分），should 表示 OR
    // 高亮处理：可对搜索结果中匹配的关键词加 <em> 标签（用 HighlightBuilder 配合 withHighlightFields）
    public List<ProductDocument> searchProduct(String keyword, int page, int size) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // boolQuery().should()：任一 should 子句匹配即返回结果，每个 should 有自己的评分
        queryBuilder.withQuery(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("name", keyword))        // name 字段分词匹配
                .should(QueryBuilders.matchQuery("description", keyword))); // description 字段分词匹配
        queryBuilder.withPageable(PageRequest.of(page, size));
        SearchHits<ProductDocument> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(), ProductDocument.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    public ProductDocument syncProduct(ProductDocument product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
