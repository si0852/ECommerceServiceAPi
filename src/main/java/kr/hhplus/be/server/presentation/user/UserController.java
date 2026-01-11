package kr.hhplus.be.server.presentation.user;


import jakarta.validation.Valid;
import kr.hhplus.be.server.application.UserPointFacade;
import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.presentation.dto.request.UserChargePointRequest;
import kr.hhplus.be.server.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserPointFacade userPointFacade;

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<UserResponse>> chargePoint(@Valid @RequestBody UserChargePointRequest request) {
        UserResponse userResponse = userPointFacade.chargePoint(request.userId(), request.chargePoint());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(userResponse));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserPoint(@PathVariable("userId") String userId) {
        UserResponse userResponse = userPointFacade.getPoint(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(userResponse));
    }
}
