package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
public class CommunityController {
    private CommunityService communityService;

    // 전체 커뮤니티 정보
    @GetMapping
    public ApiResponse<Page<CommunityListResponse>> getCommunities(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int limit
    ){
        Page<CommunityListResponse> communities = communityService.getCommunities(page, limit);
        return ApiResponse.ok(communities);
    }

    @PostMapping
    public ApiResponse<CommunityResponse> createCommunity(
            Authentication authentication,

            @RequestParam
            @Valid
            CreateCommunityRequest request
    ){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); //로그인 유저 정보
        CommunityResponse newCommunity = communityService.createCommunity(userDetails.getUsername(), request);
        return ApiResponse.ok(newCommunity, "new roots are made!");
    }

    // 특정 커뮤니티 정보
    @GetMapping("/{communityId}")
    public ApiResponse<CommunityResponse> getCommunityById(
            @PathVariable Long communityId
    ){
        CommunityResponse community = communityService.getCommunityById(communityId);
        return ApiResponse.ok(community);
    }

    @PostMapping("/{communityId}/posts")
    public ApiResponse<> createCommunityPost(){

    }

    @GetMapping("/{communityId}/posts")
    public ApiResponse<> getCommunityPost(){

    }

    @GetMapping("/{communityId}/posts/{postId}")
    public ApiResponse<> getCommunityPostById(
            @PathVariable Long postId
    ){

    }

    @PutMapping("/{communityId}/posts/{postId}")
    public ApiResponse<> updateCommunityPost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody @Vaild UpdateCommunityPostRequest request
    ){
        UserDetails userDetails = (UserDetails)authentication.getPrincipal();

    }

    @DeleteMapping("/{communityId}/posts/{postId}")
    public ApiResponse<> DeleteCommunityPost(
            Authentication authentication,
            @PathVariable Long postId
    ){

    }






}
