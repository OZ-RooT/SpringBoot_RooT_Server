package io.github._3xhaust.root_server.domain.history.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id,created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String keyword;

    @Column(name = "garage_sale_id")
    private Long garageSaleId;

    @Column(name = "product_id")
    private Long productId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public SearchHistory(User user, String keyword, Long garageSaleId, Long productId) {
        this.user = user;
        this.keyword = keyword;
        this.garageSaleId = garageSaleId;
        this.productId = productId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
