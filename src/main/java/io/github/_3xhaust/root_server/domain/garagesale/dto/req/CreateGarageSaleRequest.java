package io.github._3xhaust.root_server.domain.garagesale.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGarageSaleRequest {

    @NotBlank(message = "개러지 세일 이름은 필수입니다.")
    private String name;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    @NotNull(message = "시작 시간은 필수입니다.")
    private Instant startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private Instant endTime;
}
