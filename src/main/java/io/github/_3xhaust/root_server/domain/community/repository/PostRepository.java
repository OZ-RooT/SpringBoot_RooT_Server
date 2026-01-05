package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCommunityId(Long communityId, Pageable pageable);

    Optional<Post> findByIdAndDeletedFalse(Long postId);

    Page<Post> findByCommunityIdAndDeletedFalse(Long communityId, Pageable pageable);
//    List<Post> findAllByDeletedFalse();
}

