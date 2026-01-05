package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityRequest {

    @NotBlank(message = "커뮤니티 이름은 빌 수 없습니다.")
    @Pattern(regexp = "^[A-Za-z]+$", message = "community name english ok")
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank(message = "설명을 작성해주세요")
    @Size(min = 10, max = 150)
    private String description;

}
