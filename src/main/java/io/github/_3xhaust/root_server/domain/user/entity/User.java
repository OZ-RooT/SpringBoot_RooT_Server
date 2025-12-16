package io.github._3xhaust.root_server.domain.user.entity;

import io.github._3xhaust.root_server.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Short rating;

    @Column(length = 5)
    private String language;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public User(String email, String password, String name, Image profileImage, Short rating, String language) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profileImage = profileImage;
        this.rating = rating != null ? rating : 5;
        this.language = language;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.rating == null) {
            this.rating = 5;
        }
    }

    public boolean checkPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name, String language, Image profileImage) {
        if (name != null) {
            this.name = name;
        }
        if (language != null) {
            this.language = language;
        }
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return this.name;
    }
}