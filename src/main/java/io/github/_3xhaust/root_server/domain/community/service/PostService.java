package io.github._3xhaust.root_server.domain.community.service;

//import co.elastic.clients.elasticsearch.connector.PostResponse; //이거 부르시면 안되.요.

import io.github._3xhaust.root_server.domain.community.dto.req.CreatePostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdatePostRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.PostListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.PostResponse;
import io.github._3xhaust.root_server.domain.community.entity.Community;
import io.github._3xhaust.root_server.domain.community.entity.Post;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.community.repository.PostRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;

    //조회
    public PostResponse getPostById(Long communityId, Long postId){
        return postRepository.findById(postId)
                .map(PostResponse::of) // Optional 안의 Post를 PostResponse로 변환
                .orElseThrow(() -> new IllegalArgumentException("not exist post, id: " + postId));
    }


    // post 전체 조회
    public Page<PostListResponse> getPosts(Long communityId, int page, int limit) {

        if (!communityRepository.existsById(communityId)) {
            throw new IllegalArgumentException("not grown community yet, id : "+ communityId);
        }

        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        return postRepository.findByCommunityIdAndDeletedFalse(communityId, pageRequest)
                .map(PostListResponse::of); // 엔티티를 DTO로 변환하는 정적 메서드 활용
    }

    // post 생성
    @Transactional
    public Long createPost(String userName, Long communityId, CreatePostRequest request){
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커뮤니티입니다."));

        boolean isAuthorExist = userRepository.existsByEmail(userName);

        if(!isAuthorExist){
            throw new IllegalArgumentException("존재하지 않는 유저의 접근.");
        }

        User author = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        // 4. DTO -> Entity 변환
        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .authorId(author.getId())
                .communityId(communityId)
                .build();

        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    @Transactional
    public void updateCommunityPost(Long communityId, Long postId, String reqUsername, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

//        if (!post.getCommunityId().equals(communityId)) {
//            throw new IllegalArgumentException("communityId mismatch");
//        }

        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("author not found"));

        if (!author.getName().equals(reqUsername)) {
            throw new SecurityException("no permission");
        }

        post.update(request.getTitle(), request.getBody());
    }

    @Transactional
    public void deleteCommunityPost(String userEmail, Long postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        if(post.isDeleted()) return;

        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("author not found"));

        if (!author.getEmail().equals(userEmail)) {
            throw new SecurityException("no permission");
        }

        post.delete();
    }
}
