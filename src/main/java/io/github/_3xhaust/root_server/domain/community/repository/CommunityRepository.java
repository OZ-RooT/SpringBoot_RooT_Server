package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findFirstByName(String name);

    Page<Community> findByOwnerId(Long ownerId, Pageable pageable);
}
