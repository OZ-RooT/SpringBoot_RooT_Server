package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.service.CommunityService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
public class CommunityController {
    private CommunityService communityService;

    // 전체 커뮤니티 정보
    @GetMapping
    public ApiResponse<> getCommunity(){

    }

    @PostMapping
    public ApiResponse<T> createCommunity(
            @RequestParam() String name
    ){

    }

    // 특정 커뮤니티 정보
    @GetMapping("/{communityId}")
    public ApiResponse getCommunityById(
            @PathVariable Long communityId
    ){

    }

    @PostMapping("/{communityId}/posts")
    public ApiResponse createCommunityPost(){

    }

    @GetMapping("/{communityId}/posts")
    public ApiResponse getCommunityPost(){

    }

    @GetMapping("/{communityId}/posts/{postId}")
    public ApiResponse getCommunityPostById(
            @PathVariable Long postId
    ){

    }

    @PutMapping("/{communityId}/posts/{postId}")
    public ApiResponse updateCommunityPost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody @Vaild UpdateCommunityPostRequest request
    ){
        UserDetails userDetails = (UserDetails)authentication.getPrincipal();

    }

    @DeleteMapping("/{communityId}/posts/{postId}")
    public ApiResponse DeleteCommunityPost(
            Authentication authentication,
            @PathVariable Long postId
    ){

    }






}
