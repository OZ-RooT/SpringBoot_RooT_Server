package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {
    private Long id; // createdBy
    private CreatedByInfo createdBy;
    private String name;
    private String description;
    private Integer point;
    private Short gradeLevel;

    private Instant createdAt;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedByInfo{
        private Long id;
        private String name;
    }

    // entity -> DTO, from과 차이가 거의 없음
    public static CommunityResponse of (Community community){
        // builder 사용으로 간략화
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .createdBy(CreatedByInfo.builder()
                        .id(community.getCreatedBy().getId())
                        .name(community.getCreatedBy().getName())
                        .build())
                .createdAt(community.getCreatedAt())
                .build();
    }

}
