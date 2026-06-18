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
public class GarageSaleListResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<String> imageUrls;
    private OwnerInfo owner;
    private Integer productCount;
    private Instant createdAt;
    private List<String> tags;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String name;
    }

    public static GarageSaleListResponse of(GarageSale garageSale) {
        return GarageSaleListResponse.builder()
                .id(garageSale.getId())
                .name(garageSale.getName())
                .latitude(garageSale.getLatitude())
                .longitude(garageSale.getLongitude())
                .startDate(garageSale.getStartDate())
                .endDate(garageSale.getEndDate())
                .startTime(garageSale.getStartTime())
                .endTime(garageSale.getEndTime())
                .imageUrls(garageSale.getGarageSaleImages().stream()
                        .map(gi -> gi.getImage().getUrl())
                        .toList())
                .owner(OwnerInfo.builder()
                        .id(garageSale.getOwner().getId())
                        .name(garageSale.getOwner().getName())
                        .build())
                .productCount(garageSale.getProducts().size())
                .createdAt(garageSale.getCreatedAt())
                .tags(garageSale.getGarageSaleTags().stream()
                        .map(garageSaleTag -> garageSaleTag.getTag().getName())
                        .toList())
                .build();
    }
}
