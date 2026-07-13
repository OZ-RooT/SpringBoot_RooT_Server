package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityChannelRepository extends JpaRepository<CommunityChannel, Long> {
    Optional<CommunityChannel> findFirstByCommunityIdAndName(Long communityId, String name);

    List<CommunityChannel> findByCommunityId(Long communityId);

    List<CommunityChannel> findByCommunityIdAndType(Long communityId, String type);
}
