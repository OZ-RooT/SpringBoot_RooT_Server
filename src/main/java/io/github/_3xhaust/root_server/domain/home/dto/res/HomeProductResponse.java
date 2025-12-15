package io.github._3xhaust.root_server.domain.home.dto.res;

import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class HomeProductResponse {
    private Long id;
    private String title;
    private Integer price;
    private String description;
    private Short type;
    private String thumbnailUrl;
    private List<String> tags;
    private SellerInfo seller;
    private Instant createdAt;

    @Getter
    @Builder
    public static class SellerInfo {
        private Long id;
        private String name;
        private String email;
    }

    public static HomeProductResponse from(ProductDocument document) {
        return HomeProductResponse.builder()
                .id(document.getProductId())
                .title(document.getTitle())
                .price(document.getPrice())
                .description(document.getDescription())
                .type(document.getType())
                .thumbnailUrl(document.getImageUrls() != null && !document.getImageUrls().isEmpty()
                        ? document.getImageUrls().get(0) : null)
                .tags(document.getTags())
                .seller(SellerInfo.builder()
                        .id(document.getSellerId())
                        .name(document.getSellerName())
                        .email(document.getSellerEmail())
                        .build())
                .createdAt(document.getCreatedAt())
                .build();
    }
}

