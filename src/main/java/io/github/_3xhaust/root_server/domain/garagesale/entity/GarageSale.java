package io.github._3xhaust.root_server.domain.garagesale.entity;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTag;
import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "garage_sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GarageSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @OneToMany(mappedBy = "garageSale")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "garageSale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GarageSaleImage> garageSaleImages = new ArrayList<>();

    @OneToMany(mappedBy = "garageSale")
    private List<GarageSaleTag> garageSaleTags = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public GarageSale(User owner, String name, Double latitude, Double longitude, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        this.owner = owner;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String name, Double latitude, Double longitude, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addImage(GarageSaleImage garageSaleImage) {
        this.garageSaleImages.add(garageSaleImage);
    }

    public void clearImages() {
        this.garageSaleImages.clear();
    }

    public void removeImage(GarageSaleImage garageSaleImage) {
        if (garageSaleImage == null) return;

        boolean removed = this.garageSaleImages.remove(garageSaleImage);
        if (removed) return;

        final Long targetImageId = garageSaleImage.getImage() != null ? garageSaleImage.getImage().getId() : null;

        if (targetImageId != null) {
            this.garageSaleImages.removeIf(gi -> {
                if (gi == null || gi.getImage() == null) return false;
                final Long imgId = gi.getImage().getId();
                return imgId != null && imgId.equals(targetImageId);
            });
        } else {
            this.garageSaleImages.removeIf(gi -> gi.equals(garageSaleImage));
        }
    }
}
