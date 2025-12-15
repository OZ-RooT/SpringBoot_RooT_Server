package io.github._3xhaust.root_server.infrastructure.elasticsearch.repository;

import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByTypeAndIsActiveTrue(Short type, Pageable pageable);

    Page<ProductDocument> findByIsActiveTrue(Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"isActive\": true}}]}}")
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"isActive\": true}}, {\"term\": {\"type\": ?1}}]}}")
    Page<ProductDocument> searchByKeywordAndType(String keyword, Short type, Pageable pageable);

    Page<ProductDocument> findByTagsContainingAndIsActiveTrue(String tag, Pageable pageable);

    Page<ProductDocument> findByGarageSaleIdAndIsActiveTrue(Long garageSaleId, Pageable pageable);

    List<ProductDocument> findBySellerIdAndIsActiveTrue(Long sellerId);

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"isActive\": true}}, {\"terms\": {\"tags\": ?0}}]}}")
    Page<ProductDocument> findByTagsInAndIsActiveTrue(List<String> tags, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": [{\"term\": {\"isActive\": true}}, {\"term\": {\"type\": ?2}}]}}")
    Page<ProductDocument> findByPriceRangeAndType(Integer minPrice, Integer maxPrice, Short type, Pageable pageable);
}

