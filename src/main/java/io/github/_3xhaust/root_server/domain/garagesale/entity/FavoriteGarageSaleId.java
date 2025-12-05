package io.github._3xhaust.root_server.domain.garagesale.entity;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteGarageSaleId implements Serializable {
    private Long user;
    private Long garageSale;

    public FavoriteGarageSaleId() {}

    public FavoriteGarageSaleId(Long user, Long garageSale) {
        this.user = user;
        this.garageSale = garageSale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteGarageSaleId that = (FavoriteGarageSaleId) o;
        return Objects.equals(user, that.user) && Objects.equals(garageSale, that.garageSale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, garageSale);
    }
}

