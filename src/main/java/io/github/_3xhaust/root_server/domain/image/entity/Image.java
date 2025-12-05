package io.github._3xhaust.root_server.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @CreatedDate
    @Column
    private Instant createdAt;

    @Builder
    public Image(String url) {
        this.url = url;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
