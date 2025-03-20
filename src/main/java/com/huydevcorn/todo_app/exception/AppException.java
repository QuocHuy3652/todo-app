package com.huydevcorn.todo_app.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Custom exception class for application-specific errors.
 */
@Getter
@Setter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;

    /**
     * Constructs a new AppException with the specified error code.
     *
     * @param errorCode the error code representing the specific error
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
