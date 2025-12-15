package io.github._3xhaust.root_server.domain.home.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeFeedResponse {
    private List<HomeProductResponse> usedProducts;
    private List<HomeGarageSaleResponse> garageSales;
    private List<HomeGarageSaleResponse> nearbyGarageSales;
    private PageInfo pageInfo;

    @Getter
    @Builder
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}

