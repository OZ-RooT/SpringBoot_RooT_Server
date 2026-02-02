package io.github._3xhaust.root_server.infrastructure.elasticsearch.repository;

import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.util.List;

public interface GarageSaleSearchRepository extends ElasticsearchRepository<GarageSaleDocument, String> {

    Page<GarageSaleDocument> findByIsActiveTrue(Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"isActive\": true}}]}}")
    Page<GarageSaleDocument> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"isActive\": true}}, {\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}]}}")
    Page<GarageSaleDocument> findByLocationNear(Double latitude, Double longitude, Double distanceKm, Pageable pageable);

    Page<GarageSaleDocument> findByTagsContainingAndIsActiveTrue(String tag, Pageable pageable);

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"isActive\": true}}, {\"range\": {\"startTime\": {\"lte\": ?0}}}, {\"range\": {\"endTime\": {\"gte\": ?0}}}]}}")
    Page<GarageSaleDocument> findCurrentlyActive(Instant now, Pageable pageable);

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"isActive\": true}}, {\"range\": {\"startTime\": {\"gte\": ?0, \"lte\": ?1}}}]}}")
    Page<GarageSaleDocument> findByDateRange(Instant startDate, Instant endDate, Pageable pageable);

    List<GarageSaleDocument> findByOwnerIdAndIsActiveTrue(Long ownerId);

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"isActive\": true}}, {\"terms\": {\"tags\": ?0}}]}}")
    Page<GarageSaleDocument> findByTagsInAndIsActiveTrue(List<String> tags, Pageable pageable);

    @Query("{\"bool\": {" +
           "\"filter\": [" +
           "{\"term\": {\"isActive\": true}}," +
           "{\"terms\": {\"garageSaleId\": ?0}}" +
           "]" +
           "}}")
    Page<GarageSaleDocument> findRecommendedGarageSalesByIds(List<Long> ids, Pageable pageable);
}

