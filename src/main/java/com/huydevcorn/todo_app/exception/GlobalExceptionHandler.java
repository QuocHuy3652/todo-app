package com.huydevcorn.todo_app.exception;

import com.huydevcorn.todo_app.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<String>> handlingException() {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handlingAppException(AppException appException) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setCode(appException.getErrorCode().getCode());
        apiResponse.setMessage(appException.getErrorCode().getMessage());
        return ResponseEntity.status(appException.getErrorCode().getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception) {
        String errorKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        var constraintViolation =
                exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes =
                constraintViolation.getConstraintDescriptor().getAttributes();

        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(ErrorCode.valueOf(errorKey).getCode());
        response.setMessage(mapAttribute(ErrorCode.valueOf(errorKey).getMessage(), attributes));
        return ResponseEntity.status(ErrorCode.valueOf(errorKey).getHttpStatusCode())
                .body(response);
    }

    private String mapAttribute(String message, Map<String, Object> attribute) {
        String minValue = String.valueOf(attribute.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
