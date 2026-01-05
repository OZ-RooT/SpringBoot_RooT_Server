package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> { //extends JpaRepository
    // 기본 crud 메서드 제공

    @Override
    Optional<Community> findById(Long communityId);

    boolean existByName(String name);

    //    boolean deleteById(Long communityId);

}
