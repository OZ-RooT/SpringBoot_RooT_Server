package io.github._3xhaust.root_server.domain.recommendation.controller;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.recommendation.service.RecommendationService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendations", description = "Personalized recommendations and trending content for Australian marketplace")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "Get recommended garage sales",
            description = "Returns personalized garage sale recommendations based on user's recent views, searches, and favorites within the last 30 days. Optimized for Australian locations.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved recommended garage sales",
                    content = @Content(schema = @Schema(implementation = GarageSaleListResponse.class))
            )
    })
    @GetMapping("/garage-sales")
    public ApiResponse<Page<GarageSaleListResponse>> recommendGarageSales(
            @Parameter(description = "User authentication token", hidden = true) Authentication authentication,
            @Parameter(description = "User's latitude in Australia (e.g., -33.8688 for Sydney)", example = "-33.8688") @RequestParam(required = false) Double latitude,
            @Parameter(description = "User's longitude in Australia (e.g., 151.2093 for Sydney)", example = "151.2093") @RequestParam(required = false) Double longitude,
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        String userEmail = authentication != null
                ? ((UserDetails) authentication.getPrincipal()).getUsername()
                : null;

        Page<GarageSaleListResponse> recommendations = recommendationService.recommendGarageSales(
                userEmail, latitude, longitude, page, limit
        );
        return ApiResponse.ok(recommendations);
    }

    @Operation(
            summary = "Get recommended products",
            description = "Returns personalized second-hand product recommendations based on user's recent views, searches, and favorites within the last 30 days. Optimized for Australian marketplace with AUD pricing.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved recommended products",
                    content = @Content(schema = @Schema(implementation = ProductListResponse.class))
            )
    })
    @GetMapping("/products")
    public ApiResponse<Page<ProductListResponse>> recommendProducts(
            @Parameter(description = "User authentication token", hidden = true) Authentication authentication,
            @Parameter(description = "User's latitude in Australia", example = "-37.8136") @RequestParam(required = false) Double latitude,
            @Parameter(description = "User's longitude in Australia", example = "144.9631") @RequestParam(required = false) Double longitude,
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        String userEmail = authentication != null
                ? ((UserDetails) authentication.getPrincipal()).getUsername()
                : null;

        Page<ProductListResponse> recommendations = recommendationService.recommendProducts(
                userEmail, latitude, longitude, page, limit
        );
        return ApiResponse.ok(recommendations);
    }

    @Operation(
            summary = "Get trending garage sales",
            description = "Returns trending garage sales across all users in Australia, ranked by popularity (favorites × 2 + views). No authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved trending garage sales",
                    content = @Content(schema = @Schema(implementation = GarageSaleListResponse.class))
            )
    })
    @GetMapping("/garage-sales/trending")
    public ApiResponse<Page<GarageSaleListResponse>> trendingGarageSales(
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        Page<GarageSaleListResponse> trending = recommendationService.trendingGarageSales(page, limit);
        return ApiResponse.ok(trending);
    }

    @Operation(
            summary = "Get trending products",
            description = "Returns trending second-hand products across all users in Australia, ranked by popularity (favorites × 2 + views). No authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved trending products",
                    content = @Content(schema = @Schema(implementation = ProductListResponse.class))
            )
    })
    @GetMapping("/products/trending")
    public ApiResponse<Page<ProductListResponse>> trendingProducts(
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        Page<ProductListResponse> trending = recommendationService.trendingProducts(page, limit);
        return ApiResponse.ok(trending);
    }

    @Operation(
            summary = "Get popular garage sale tags",
            description = "Returns the top popular tags used in garage sales across Australia. Tags are ranked by usage frequency and recent activity."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved popular garage sale tags",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping("/garage-sales/tags")
    public ApiResponse<List<String>> getPopularGarageSaleTags(
            @Parameter(description = "Maximum number of tags to return", example = "12") @RequestParam(defaultValue = "12") int limit
    ) {
        List<String> tags = recommendationService.getPopularGarageSaleTags(limit);
        return ApiResponse.ok(tags);
    }

    @Operation(
            summary = "Get popular product tags",
            description = "Returns the top popular tags used in second-hand products across Australia. Tags are ranked by usage frequency and recent activity."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved popular product tags",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping("/products/tags")
    public ApiResponse<List<String>> getPopularProductTags(
            @Parameter(description = "Maximum number of tags to return", example = "12") @RequestParam(defaultValue = "12") int limit
    ) {
        List<String> tags = recommendationService.getPopularProductTags(limit);
        return ApiResponse.ok(tags);
    }
}
