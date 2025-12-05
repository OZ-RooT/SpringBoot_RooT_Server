package io.github._3xhaust.root_server.domain.garagesale.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGarageSaleRequest {

    private String name;

    private Double latitude;

    private Double longitude;

    private Instant startTime;

    private Instant endTime;
}

