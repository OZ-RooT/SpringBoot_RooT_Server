package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository { //extends JpaRepository
    // 기본 crud 메서드 제공

    Page<Community> findCommunityById(Long communityId, Pageable pageable);
    List<Community> findCommunityById(Long communityId);



    boolean deleteCommunityById(Long communityId);

}
