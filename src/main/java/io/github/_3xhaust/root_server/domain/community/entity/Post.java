package io.github._3xhaust.root_server.domain.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "posts")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //게시글의 ID

//    @OneToMany()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Long communityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Long authorId;

    // 1 : 존재
    // 0 : 삭제
    // 이외 숫자 그 이외의 상태 ex. 신고 게시글이라던가
    @Column(nullable = false)
    @ColumnDefault(value = "1")
    private byte status;

    @Column
    private Instant deletedAt;

    // property
    @Column(nullable = false)
    @Max(15)
    private String title;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String body;

    @CreatedDate
    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreated() { this.createdAt = Instant.now(); }


    @Builder
    public Post(String title, String body, Long authorId, Long communityId){
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.communityId = communityId;
    }

    public void update(String title, String body){
        this.title = title;
        this.body = body;
    }

    public void delete() {
        if (this.status == 1) { // 게시글이 존재한다면
            return;
        }
        this.status = 0;
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return status != 1;
    }

}
