package io.github._3xhaust.root_server.domain.garagesale.controller;

import io.github._3xhaust.root_server.domain.garagesale.dto.req.CreateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.req.UpdateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleDetailResponse;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleResponse;
import io.github._3xhaust.root_server.domain.garagesale.service.GarageSaleService;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/garage-sales")
@RequiredArgsConstructor
public class GarageSaleController {

    private final GarageSaleService garageSaleService;

    @GetMapping
    public ApiResponse<List<GarageSaleListResponse>> getAllGarageSales(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "10") Double radius
    ) {
        List<GarageSaleListResponse> garageSales = garageSaleService.getAllGarageSales(lat, lng, radius);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/{id}")
    public ApiResponse<GarageSaleResponse> getGarageSaleById(@PathVariable Long id) {
        GarageSaleResponse garageSale = garageSaleService.getGarageSaleById(id);
        return ApiResponse.ok(garageSale);
    }

    @PostMapping
    public ApiResponse<GarageSaleResponse> createGarageSale(
            Authentication authentication,
            @RequestBody @Valid CreateGarageSaleRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        GarageSaleResponse garageSale = garageSaleService.createGarageSale(userDetails.getUsername(), request);
        return ApiResponse.ok(garageSale, "개러지 세일이 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<GarageSaleResponse> updateGarageSale(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid UpdateGarageSaleRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        GarageSaleResponse garageSale = garageSaleService.updateGarageSale(userDetails.getUsername(), id, request);
        return ApiResponse.ok(garageSale, "개러지 세일이 수정되었습니다.");
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> toggleFavoriteGarageSale(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        garageSaleService.toggleFavoriteGarageSale(userDetails.getUsername(), id);
        return ApiResponse.ok((Void) null, "즐겨찾기가 변경되었습니다.");
    }

    @GetMapping("/{id}/{productId}")
    public ApiResponse<ProductResponse> getGarageSaleProduct(
            @PathVariable Long id,
            @PathVariable Long productId
    ) {
        ProductResponse product = garageSaleService.getGarageSaleProduct(id, productId);
        return ApiResponse.ok(product);
    }

    @PostMapping("/{id}/{productId}/favorite")
    public ApiResponse<Void> toggleFavoriteGarageSaleProduct(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long productId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        garageSaleService.toggleFavoriteGarageSaleProduct(userDetails.getUsername(), id, productId);
        return ApiResponse.ok((Void) null, "관심 상품이 변경되었습니다.");
    }

    // Elasticsearch 기반 검색 엔드포인트
    @GetMapping("/search")
    public ApiResponse<Page<GarageSaleListResponse>> getGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<GarageSaleListResponse> garageSales = garageSaleService.getGarageSalesFromElasticsearch(page, limit, sortBy, sortDir);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<GarageSaleDetailResponse> getGarageSaleDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int productPage,
            @RequestParam(defaultValue = "20") int productLimit
    ) {
        GarageSaleDetailResponse response = garageSaleService.getGarageSaleDetailFromElasticsearch(id, productPage, productLimit);
        return ApiResponse.ok(response);
    }

    @GetMapping("/nearby")
    public ApiResponse<Page<GarageSaleListResponse>> getNearbyGarageSales(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<GarageSaleListResponse> garageSales = garageSaleService.searchNearbyGarageSalesFromElasticsearch(
                latitude, longitude, radiusKm, page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/current")
    public ApiResponse<Page<GarageSaleListResponse>> getCurrentGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<GarageSaleListResponse> garageSales = garageSaleService.getCurrentGarageSalesFromElasticsearch(page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/schedule")
    public ApiResponse<Page<GarageSaleListResponse>> getGarageSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<GarageSaleListResponse> garageSales = garageSaleService.getGarageSalesByDateRangeFromElasticsearch(
                startDate, endDate, page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/tags")
    public ApiResponse<Page<GarageSaleListResponse>> getGarageSalesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<GarageSaleListResponse> garageSales = garageSaleService.getGarageSalesByTagFromElasticsearch(tag, page, limit);
        return ApiResponse.ok(garageSales);
    }
}

