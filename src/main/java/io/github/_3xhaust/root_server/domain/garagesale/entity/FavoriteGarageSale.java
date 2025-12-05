package io.github._3xhaust.root_server.domain.garagesale.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_garage_sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(FavoriteGarageSaleId.class)
public class FavoriteGarageSale {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_sale_id")
    private GarageSale garageSale;

    @Builder
    public FavoriteGarageSale(User user, GarageSale garageSale) {
        this.user = user;
        this.garageSale = garageSale;
    }
}

