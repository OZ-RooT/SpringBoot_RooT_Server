package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "community")
@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO : 오류 가능성 잇음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_id", nullable = false)
    private User createdBy;

    // 커뮤니티 이름
    @Column(nullable = false)
    private String name;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    // 커뮤니티 성장 타입
    @Column(nullable = false)
    private Integer point;

    @Column(nullable = false)
    private Integer gradeLevel;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;


    // crud
    @Builder
    public Community(
            User createdBy,
            String name,
            String description,
            Integer point,
            Integer gradeLevel
    ){
      this.createdBy = createdBy;
      this.name = name;
      this.description = description;
      this.point = point;
      this.gradeLevel = gradeLevel;
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }

    public void update(String name, String description, Integer point, Integer gradeLevel){
        this.name = name;
        this.description = description;
        this.point = point;
        this.gradeLevel = gradeLevel;
    }


}
