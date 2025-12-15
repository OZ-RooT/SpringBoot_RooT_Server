package io.github._3xhaust.root_server.domain.home.dto.res;

import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class HomeGarageSaleResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Instant startTime;
    private Instant endTime;
    private List<String> tags;
    private Integer productCount;
    private OwnerInfo owner;
    private Instant createdAt;
    private Double distance; // km 단위, 위치 기반 검색 시에만

    @Getter
    @Builder
    public static class OwnerInfo {
        private Long id;
        private String name;
        private String email;
    }

    public static HomeGarageSaleResponse from(GarageSaleDocument document) {
        return HomeGarageSaleResponse.builder()
                .id(document.getGarageSaleId())
                .name(document.getName())
                .latitude(document.getLatitude())
                .longitude(document.getLongitude())
                .startTime(document.getStartTime())
                .endTime(document.getEndTime())
                .tags(document.getTags())
                .productCount(document.getProductCount())
                .owner(OwnerInfo.builder()
                        .id(document.getOwnerId())
                        .name(document.getOwnerName())
                        .email(document.getOwnerEmail())
                        .build())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public static HomeGarageSaleResponse from(GarageSaleDocument document, Double distance) {
        return HomeGarageSaleResponse.builder()
                .id(document.getGarageSaleId())
                .name(document.getName())
                .latitude(document.getLatitude())
                .longitude(document.getLongitude())
                .startTime(document.getStartTime())
                .endTime(document.getEndTime())
                .tags(document.getTags())
                .productCount(document.getProductCount())
                .owner(OwnerInfo.builder()
                        .id(document.getOwnerId())
                        .name(document.getOwnerName())
                        .email(document.getOwnerEmail())
                        .build())
                .createdAt(document.getCreatedAt())
                .distance(distance)
                .build();
    }
}

