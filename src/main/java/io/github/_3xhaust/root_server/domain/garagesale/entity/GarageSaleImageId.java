package io.github._3xhaust.root_server.domain.garagesale.entity;

import java.io.Serializable;
import java.util.Objects;

public class GarageSaleImageId implements Serializable {
    private Long garageSale;
    private Long image;

    public GarageSaleImageId() {}

    public GarageSaleImageId(Long garageSale, Long image) {
        this.garageSale = garageSale;
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarageSaleImageId that = (GarageSaleImageId) o;
        return Objects.equals(garageSale, that.garageSale) && Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(garageSale, image);
    }
}
