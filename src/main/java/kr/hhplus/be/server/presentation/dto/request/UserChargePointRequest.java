package kr.hhplus.be.server.presentation.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UserChargePointRequest(
        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,

        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        Long chargePoint
) {
}
