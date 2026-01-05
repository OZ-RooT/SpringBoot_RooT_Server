package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.CreatePostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdatePostRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.PostListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.PostResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityService;
import io.github._3xhaust.root_server.domain.community.service.PostService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// TODO : communtiy 관련 이미지 생성 로직
// TODO : 이미지 삽입에 따른 DB 변경 (서버 동작 확인 후)

@RestController
@RequestMapping("/api/v1/community")
public class CommunityController {
    private CommunityService communityService;
    private PostService postService; // 헤깔림 주의

    // 전체 커뮤니티 정보
    @GetMapping
    public ApiResponse<Page<CommunityListResponse>> getCommunities(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int limit
    ){
        Page<CommunityListResponse> communities = communityService.getCommunities(page-1, limit);
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

    // 커뮤니티의 전체 게시글 정보
    @GetMapping("/{communityId}/posts")
    public ApiResponse<Page<PostListResponse>> createCommunityPost(
            @PathVariable Long communityId,
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int limit
    ){
        Page<PostListResponse> postPage = postService.getPosts(communityId, page-1, limit);
        return ApiResponse.ok(postPage);
    }

    @PostMapping("/{communityId}/posts")
    public ApiResponse<Long> createPost(
            Authentication authentication,
            @PathVariable Long communityId,
            @RequestBody CreatePostRequest request // JSON 데이터를 DTO로 받음
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        Long postId = postService.createPost(userName, communityId, request);
        return ApiResponse.ok(postId);
        // 게시글 생성 후 해당 게시글 ID 반환
    }

    @GetMapping("/{communityId}/posts/{postId}")
    public ApiResponse<PostResponse> getCommunityPostById(
            @PathVariable Long communityId,
            @PathVariable Long postId
    ){
        PostResponse post = postService.getPostById(communityId, postId);
        return ApiResponse.ok(post);
    }

    @PutMapping("/{communityId}/posts/{postId}")
    public ApiResponse<Void> updateCommunityPost(
            Authentication authentication,
            @PathVariable Long communityId,
            @PathVariable Long postId,
            @RequestBody @Valid UpdatePostRequest request
    ){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        postService.updateCommunityPost(communityId, postId, username, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{communityId}/posts/{postId}")
    public ApiResponse<Boolean> DeleteCommunityPost(
            Authentication authentication,
            @PathVariable Long postId
    ){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        postService.deleteCommunityPost(username, postId);
        return ApiResponse.ok(null);
    }

}
