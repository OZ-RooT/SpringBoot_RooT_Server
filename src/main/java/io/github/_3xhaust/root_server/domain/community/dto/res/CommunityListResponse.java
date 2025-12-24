package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityListResponse {
    private Long id; // createdBy
    private String name;
    private String description;
//    private CreatedByInfo createdBy;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedByInfo{
        private Long id;
        private String name;
    }

    public static CommunityListResponse of (Community community){

        return CommunityListResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
//                .createdBy(CreatedByInfo.builder()
//                        .id(community.getCreatedBy().getId())
//                        .name(community.getCreatedBy().getName())
//                        .build())
                .build();
    }
}
