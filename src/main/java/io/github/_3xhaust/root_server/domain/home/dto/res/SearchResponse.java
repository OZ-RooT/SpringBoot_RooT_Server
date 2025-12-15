package io.github._3xhaust.root_server.domain.home.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<HomeProductResponse> products;
    private List<HomeGarageSaleResponse> garageSales;
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

