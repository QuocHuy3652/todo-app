package com.huydevcorn.todo_app.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    TITLE_IS_REQUIRED(HttpStatus.BAD_REQUEST.value(), "Title is required", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(HttpStatus.BAD_REQUEST.value(), "Status must be 'PENDING', 'IN_PROGRESS', or 'DONE'", HttpStatus.BAD_REQUEST),
    INVALID_PRIORITY(HttpStatus.BAD_REQUEST.value(), "Priority must be 'LOW', 'MEDIUM', 'HIGH', 'URGENT', or 'CRITICAL'", HttpStatus.BAD_REQUEST),

    TASK_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Task not found", HttpStatus.NOT_FOUND),

    INVALID_PAGE(HttpStatus.BAD_REQUEST.value(), "Page must be greater than or equal to 1", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST.value(), "Page size must be greater than or equal to 1", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
