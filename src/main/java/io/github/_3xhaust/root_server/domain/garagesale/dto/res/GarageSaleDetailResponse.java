package io.github._3xhaust.root_server.domain.garagesale.dto.res;

import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GarageSaleDetailResponse {
    private GarageSaleListResponse garageSale;
    private List<ProductListResponse> products;
    private PageInfo pageInfo;

    @Getter
    @Builder
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
    }
}

