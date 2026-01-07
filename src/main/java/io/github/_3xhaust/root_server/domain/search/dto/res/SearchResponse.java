package io.github._3xhaust.root_server.domain.search.dto.res;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<ProductListResponse> products;
    private List<GarageSaleListResponse> garageSales;
    private String keyword;
    private SearchPageInfo pageInfo;

    @Getter
    @Builder
    public static class SearchPageInfo {
        private int productPage;
        private int productTotalPages;
        private long productTotalElements;
        private int garageSalePage;
        private int garageSaleTotalPages;
        private long garageSaleTotalElements;
    }
}

