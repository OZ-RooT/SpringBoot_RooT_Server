package io.github._3xhaust.root_server.domain.search.controller;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.service.GarageSaleService;
import io.github._3xhaust.root_server.domain.history.service.HistoryService;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.service.ProductService;
import io.github._3xhaust.root_server.domain.search.dto.res.SearchResponse;
import io.github._3xhaust.root_server.domain.user.service.UserService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final GarageSaleService garageSaleService;
    private final HistoryService historyService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<SearchResponse> search(
            Authentication authentication,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm
    ) {
        String userName = authenticatedUserName(authentication);
        historyService.recordSearch(userName, keyword, null, null);

        Long userId = userName != null
                ? userService.getUserByName(userName).getId()
                : null;
        Page<ProductListResponse> productPage = productService.searchUsedProductsFromElasticsearch(keyword, page, limit, null, null, userId, latitude, longitude, radiusKm);
        Page<GarageSaleListResponse> garageSalePage = garageSaleService.searchGarageSalesByKeyword(keyword, page, limit, latitude, longitude, radiusKm);

        SearchResponse response = SearchResponse.builder()
                .keyword(keyword)
                .products(productPage.getContent())
                .garageSales(garageSalePage.getContent())
                .pageInfo(SearchResponse.SearchPageInfo.builder()
                        .productPage(page)
                        .productTotalPages(productPage.getTotalPages())
                        .productTotalElements(productPage.getTotalElements())
                        .garageSalePage(page)
                        .garageSaleTotalPages(garageSalePage.getTotalPages())
                        .garageSaleTotalElements(garageSalePage.getTotalElements())
                        .build())
                .build();

        return ApiResponse.ok(response);
    }

    @GetMapping("/latest")
    public ApiResponse<List<String>> latestSearches(
            Authentication authentication,
            @RequestParam(defaultValue = "8") int limit
    ) {
        String userName = authenticatedUserName(authentication);
        return ApiResponse.ok(historyService.getLatestSearchKeywords(userName, normalizedLimit(limit)));
    }

    @GetMapping("/top")
    public ApiResponse<List<String>> topSearches(
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ApiResponse.ok(historyService.getTopSearchKeywords(normalizedLimit(limit)));
    }

    private int normalizedLimit(int limit) {
        return Math.max(1, Math.min(limit, 20));
    }

    private String authenticatedUserName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
