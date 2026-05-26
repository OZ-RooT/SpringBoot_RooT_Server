package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostListResponse {
    private Long id;
    private Long channelId;
    private String channelName;
    private String channelType;
    private String title;
    private String body;
    private String thumbnailUrl;
    private String videoUrl;
    private AuthorInfo author;
    private ReactionCount reactionCount;
    private Integer commentCount;
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionCount {
        private Long likeCount;
        private Long dislikeCount;
    }

    public static CommunityPostListResponse of(CommunityPost post) {
        long likeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == 1)
                .count();
        long dislikeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == -1)
                .count();

        String thumbnailUrl = post.getPostImages().stream()
                .map(pi -> pi.getImage().getUrl())
                .filter(url -> !isVideoUrl(url))
                .findFirst()
                .orElse(null);
        String videoUrl = post.getPostImages().stream()
                .map(pi -> pi.getImage().getUrl())
                .filter(CommunityPostListResponse::isVideoUrl)
                .findFirst()
                .orElse(null);

        return CommunityPostListResponse.builder()
                .id(post.getId())
                .channelId(post.getChannel().getId())
                .channelName(post.getChannel().getName())
                .channelType(post.getChannel().getType())
                .title(post.getTitle())
                .body(post.getBody() != null && post.getBody().length() > 100 ?
                        post.getBody().substring(0, 100) + "..." : post.getBody())
                .thumbnailUrl(thumbnailUrl)
                .videoUrl(videoUrl)
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .name(post.getAuthor().getName())
                        .profileImageUrl(post.getAuthor().getProfileImage() != null ?
                                post.getAuthor().getProfileImage().getUrl() : null)
                        .build())
                .reactionCount(ReactionCount.builder()
                        .likeCount(likeCount)
                        .dislikeCount(dislikeCount)
                        .build())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private static boolean isVideoUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains("/videos/") ||
                lower.endsWith(".mp4") ||
                lower.endsWith(".mov") ||
                lower.endsWith(".m4v") ||
                lower.endsWith(".webm") ||
                lower.endsWith(".avi") ||
                lower.endsWith(".mkv");
    }
}
