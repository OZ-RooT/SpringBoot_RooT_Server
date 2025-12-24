package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    // repo 의존성
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;


    // 조회
    public Page<CommunityListResponse> findCommunities(int page, int limit){
        Pageable pageable =
                PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CommunityListResponse> communities = CommunityRepository.findAll(pageable);
        return communities;
    }

    // 커뮤니티 생성
    @Transactional
    public void createCommunity(){

    }

    // 커뮤니티 삭제
    // 테스트 완료 후 안정성 확정 후 구현
    public void deleteCommunity(Long communityId){

    }

}
