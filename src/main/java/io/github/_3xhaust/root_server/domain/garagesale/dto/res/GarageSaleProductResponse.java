package io.github._3xhaust.root_server.domain.garagesale.dto.res;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSaleProductResponse {
    private Long id;
    private String title;  // 아이템 이름
    private Double price;  // 가격
    private List<String> imageUrls;  // 사진 (최대 3개)

    public static GarageSaleProductResponse of(Product product) {
        List<String> imageUrls = product.getProductImages().stream()
                .map(pi -> pi.getImage().getUrl())
                .limit(3)  // 최대 3개만
                .toList();

        return GarageSaleProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .imageUrls(imageUrls)
                .build();
    }
}
