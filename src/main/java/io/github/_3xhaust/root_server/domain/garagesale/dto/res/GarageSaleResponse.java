package io.github._3xhaust.root_server.domain.garagesale.dto.res;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSaleResponse {
    private Long id;
    private OwnerInfo owner;
    private String name;
    private Double latitude;
    private Double longitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<GarageSaleProductResponse> products;
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
        private Short rating;
    }

    public static GarageSaleResponse of(GarageSale garageSale) {
        return GarageSaleResponse.builder()
                .id(garageSale.getId())
                .owner(OwnerInfo.builder()
                        .id(garageSale.getOwner().getId())
                        .name(garageSale.getOwner().getName())
                        .profileImageUrl(garageSale.getOwner().getProfileImage() != null ?
                                garageSale.getOwner().getProfileImage().getUrl() : null)
                        .rating(garageSale.getOwner().getRating())
                        .build())
                .name(garageSale.getName())
                .latitude(garageSale.getLatitude())
                .longitude(garageSale.getLongitude())
                .startDate(garageSale.getStartDate())
                .endDate(garageSale.getEndDate())
                .startTime(garageSale.getStartTime())
                .endTime(garageSale.getEndTime())
                .products(garageSale.getProducts().stream()
                        .map(GarageSaleProductResponse::of)
                        .toList())
                .createdAt(garageSale.getCreatedAt())
                .build();
    }

    public static GarageSaleResponse ofWithoutProducts(GarageSale garageSale) {
        return GarageSaleResponse.builder()
                .id(garageSale.getId())
                .owner(OwnerInfo.builder()
                        .id(garageSale.getOwner().getId())
                        .name(garageSale.getOwner().getName())
                        .profileImageUrl(garageSale.getOwner().getProfileImage() != null ?
                                garageSale.getOwner().getProfileImage().getUrl() : null)
                        .rating(garageSale.getOwner().getRating())
                        .build())
                .name(garageSale.getName())
                .latitude(garageSale.getLatitude())
                .longitude(garageSale.getLongitude())
                .startDate(garageSale.getStartDate())
                .endDate(garageSale.getEndDate())
                .startTime(garageSale.getStartTime())
                .endTime(garageSale.getEndTime())
                .createdAt(garageSale.getCreatedAt())
                .build();
    }
}

