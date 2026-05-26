package io.github._3xhaust.root_server.domain.garagesale.entity;

import io.github._3xhaust.root_server.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "garage_sale_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(GarageSaleImageId.class)
public class GarageSaleImage {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_sale_id")
    private GarageSale garageSale;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Builder
    public GarageSaleImage(GarageSale garageSale, Image image) {
        this.garageSale = garageSale;
        this.image = image;
    }
}
