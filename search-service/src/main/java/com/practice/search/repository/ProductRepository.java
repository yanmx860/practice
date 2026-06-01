package com.practice.search.repository;

import com.practice.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/** * 商品 ES 仓库 * @author ymx * @since 2026-02-22 */
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameLike(String name);

    Page<ProductDocument> findByPriceBetween(Double min, Double max, Pageable pageable);
}
