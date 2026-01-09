package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@Getter
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        return new ApiResponse<>(status.value(), message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "요청이 성공하였습니다.", data);
    }

    public static ApiResponse<Void> error(String message, HttpStatus status) {
        return new ApiResponse<>(status.value(), message, null);
    }
}
