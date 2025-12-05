package io.github._3xhaust.root_server.domain.garagesale.dto.res;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSaleListResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Instant startTime;
    private Instant endTime;
    private OwnerInfo owner;
    private Integer productCount;
    private Instant createdAt;

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
                .startTime(garageSale.getStartTime())
                .endTime(garageSale.getEndTime())
                .owner(OwnerInfo.builder()
                        .id(garageSale.getOwner().getId())
                        .name(garageSale.getOwner().getName())
                        .build())
                .productCount(garageSale.getProducts().size())
                .createdAt(garageSale.getCreatedAt())
                .build();
    }
}

