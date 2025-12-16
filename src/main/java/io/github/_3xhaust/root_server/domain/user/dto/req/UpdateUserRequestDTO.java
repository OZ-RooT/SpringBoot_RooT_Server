package io.github._3xhaust.root_server.domain.user.dto.req;

import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateUserRequestDTO {
    private Long profileImageId;

    @Size(max = 5, message = "언어 코드는 최대 5자리까지 가능합니다.")
    private String language;

    private String name;

}
