package io.github._3xhaust.root_server.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Token refresh request payload")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
