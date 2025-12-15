package io.github._3xhaust.root_server.domain.tag.entity;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "garage_sale_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GarageSaleTag {
    @EmbeddedId
    private GarageSaleTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("garageSaleId")
    @JoinColumn(name = "garage_sale_id")
    private GarageSale garageSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Builder
    public GarageSaleTag(GarageSale garageSale, Tag tag) {
        this.id = new GarageSaleTagId(garageSale.getId(), tag.getId());
        this.garageSale = garageSale;
        this.tag = tag;
    }
}

