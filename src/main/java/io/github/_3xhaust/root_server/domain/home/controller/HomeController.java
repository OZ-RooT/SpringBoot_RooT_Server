package io.github._3xhaust.root_server.domain.home.controller;

import io.github._3xhaust.root_server.domain.home.dto.res.*;
import io.github._3xhaust.root_server.domain.home.facade.HomeFacade;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 홈 API 컨트롤러
 *
 * API Endpoints:
 *
 * 홈 피드:
 * - GET /api/v1/home/feed - 홈 피드 조회 (중고상품 + 가라지세일)
 *
 * 중고거래 (type=0):
 * - GET /api/v1/home/products/used - 중고거래 상품 목록
 * - GET /api/v1/home/products/used/search - 중고거래 상품 검색
 *
 * 가라지 세일:
 * - GET /api/v1/home/garage-sales - 가라지 세일 목록
 * - GET /api/v1/home/garage-sales/{id} - 가라지 세일 상세 (내부 상품 포함)
 * - GET /api/v1/home/garage-sales/nearby - 주변 가라지 세일
 * - GET /api/v1/home/garage-sales/current - 현재 진행 중인 가라지 세일
 * - GET /api/v1/home/garage-sales/schedule - 기간별 가라지 세일
 *
 * 통합 검색:
 * - GET /api/v1/home/search - 통합 검색 (상품 + 가라지세일)
 *
 * 태그 기반 검색:
 * - GET /api/v1/home/products/tags - 태그로 상품 검색
 * - GET /api/v1/home/garage-sales/tags - 태그로 가라지세일 검색
 */
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeFacade homeFacade;

    /**
     * 홈 피드 조회
     * 중고거래 상품(type=0)과 가라지 세일을 함께 조회
     * 위치 정보가 있으면 주변 가라지 세일도 포함
     */
    @GetMapping("/feed")
    public ApiResponse<HomeFeedResponse> getHomeFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "5") Double radiusKm
    ) {
        HomeFeedResponse response = homeFacade.getHomeFeed(page, limit, latitude, longitude, radiusKm);
        return ApiResponse.ok(response);
    }

    // ==================== 중고거래 상품 (type=0) ====================

    /**
     * 중고거래 상품 목록 조회
     */
    @GetMapping("/products/used")
    public ApiResponse<Page<HomeProductResponse>> getUsedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<HomeProductResponse> products = homeFacade.getUsedProducts(page, limit, sortBy, sortDir);
        return ApiResponse.ok(products);
    }

    /**
     * 중고거래 상품 검색
     */
    @GetMapping("/products/used/search")
    public ApiResponse<Page<HomeProductResponse>> searchUsedProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        Page<HomeProductResponse> products = homeFacade.searchUsedProducts(keyword, page, limit, minPrice, maxPrice);
        return ApiResponse.ok(products);
    }

    // ==================== 가라지 세일 ====================

    /**
     * 가라지 세일 목록 조회
     */
    @GetMapping("/garage-sales")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSales(page, limit, sortBy, sortDir);
        return ApiResponse.ok(garageSales);
    }

    /**
     * 가라지 세일 상세 조회 (내부 상품(type=1) 포함)
     */
    @GetMapping("/garage-sales/{garageSaleId}")
    public ApiResponse<GarageSaleDetailResponse> getGarageSaleDetail(
            @PathVariable Long garageSaleId,
            @RequestParam(defaultValue = "1") int productPage,
            @RequestParam(defaultValue = "20") int productLimit
    ) {
        GarageSaleDetailResponse response = homeFacade.getGarageSaleDetail(garageSaleId, productPage, productLimit);
        return ApiResponse.ok(response);
    }

    /**
     * 주변 가라지 세일 검색 (지도 기반)
     */
    @GetMapping("/garage-sales/nearby")
    public ApiResponse<Page<HomeGarageSaleResponse>> getNearbyGarageSales(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.searchNearbyGarageSales(
                latitude, longitude, radiusKm, page, limit);
        return ApiResponse.ok(garageSales);
    }

    /**
     * 현재 진행 중인 가라지 세일 조회
     */
    @GetMapping("/garage-sales/current")
    public ApiResponse<Page<HomeGarageSaleResponse>> getCurrentGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getCurrentGarageSales(page, limit);
        return ApiResponse.ok(garageSales);
    }

    /**
     * 기간별 가라지 세일 조회 (일정)
     */
    @GetMapping("/garage-sales/schedule")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSalesByDateRange(
                startDate, endDate, page, limit);
        return ApiResponse.ok(garageSales);
    }

    // ==================== 통합 검색 ====================

    /**
     * 통합 검색 (상품 + 가라지세일)
     */
    @GetMapping("/search")
    public ApiResponse<SearchResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        SearchResponse response = homeFacade.search(keyword, page, limit);
        return ApiResponse.ok(response);
    }

    // ==================== 태그 기반 검색 ====================

    /**
     * 태그로 상품 검색 (단일 태그)
     */
    @GetMapping("/products/tags")
    public ApiResponse<Page<HomeProductResponse>> getProductsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeProductResponse> products = homeFacade.getProductsByTag(tag, page, limit);
        return ApiResponse.ok(products);
    }

    /**
     * 여러 태그로 상품 검색
     */
    @GetMapping("/products/tags/multiple")
    public ApiResponse<Page<HomeProductResponse>> getProductsByTags(
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeProductResponse> products = homeFacade.getProductsByTags(tags, page, limit);
        return ApiResponse.ok(products);
    }

    /**
     * 태그로 가라지세일 검색
     */
    @GetMapping("/garage-sales/tags")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSalesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSalesByTag(tag, page, limit);
        return ApiResponse.ok(garageSales);
    }
}

