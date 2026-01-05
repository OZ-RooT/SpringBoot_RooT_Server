package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityResponse;
import io.github._3xhaust.root_server.domain.community.entity.Community;
import io.github._3xhaust.root_server.domain.community.exception.CommunityErrorCode;
import io.github._3xhaust.root_server.domain.community.exception.CommunityException;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    // repo 의존성
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    // 조회
    public Page<CommunityListResponse> getCommunities(int page, int limit){
        Pageable pageable =
                PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Community> communities = communityRepository.findAll(pageable);
        return communities.map(CommunityListResponse :: of);
    }

    public CommunityResponse getCommunityById(Long communityId){
        Community community = communityRepository.findById(communityId)
                .orElseThrow(()-> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND));
        return CommunityResponse.of(community);
    }

    // 커뮤니티 생성
    @Transactional
    public CommunityResponse createCommunity(String email, CreateCommunityRequest request){
        // 기존 커뮤니티가 있는지, 조회
        if(communityRepository.existByName(request.getName())){
            throw new IllegalArgumentException("이미 존재하는 커뮤니티 이름입니다: " + request.getName());
        }

        User communityCreater = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        // community 정보
        Community community = Community.builder()
                .createdBy(communityCreater)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Community savedCommunity = communityRepository.save(community); // 향후 확장 고려

        return CommunityResponse.of(savedCommunity);
    }

    // 커뮤니티 삭제
    // 테스트 완료 후 안정성 확정 후 구현
//    @Transactional
//    public void deleteCommunity(Long communityId){
//
//    }

}
