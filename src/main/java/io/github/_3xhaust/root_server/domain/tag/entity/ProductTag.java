package io.github._3xhaust.root_server.domain.tag.entity;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTag {
    @EmbeddedId
    private ProductTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Builder
    public ProductTag(Product product, Tag tag) {
        this.id = new ProductTagId(product.getId(), tag.getId());
        this.product = product;
        this.tag = tag;
    }
}

