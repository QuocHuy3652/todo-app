package com.huydevcorn.todo_app.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Enum representing various error codes and their associated HTTP status codes and messages.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    // General errors
    UNCATEGORIZED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation errors
    TITLE_IS_REQUIRED(HttpStatus.BAD_REQUEST.value(), "Title is required", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(HttpStatus.BAD_REQUEST.value(), "Status must be 'PENDING', 'IN_PROGRESS', or 'DONE'", HttpStatus.BAD_REQUEST),
    INVALID_PRIORITY(HttpStatus.BAD_REQUEST.value(), "Priority must be 'LOW', 'MEDIUM', 'HIGH', 'URGENT', or 'CRITICAL'", HttpStatus.BAD_REQUEST),

    // Task-related errors
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Task not found", HttpStatus.NOT_FOUND),

    // Pagination errors
    INVALID_PAGE(HttpStatus.BAD_REQUEST.value(), "Page must be greater than or equal to 1", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST.value(), "Page size must be greater than or equal to 1", HttpStatus.BAD_REQUEST),

    // Dependency-related errors
    DEPENDENT_TASK_IDS_ARE_REQUIRED(HttpStatus.BAD_REQUEST.value(), "Dependent task ids are required", HttpStatus.BAD_REQUEST),
    TASK_CANNOT_DEPEND_ON_ITSELF(HttpStatus.BAD_REQUEST.value(), "Task cannot depend on itself", HttpStatus.BAD_REQUEST),
    DEPENDENT_TASK_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Dependent task not found", HttpStatus.NOT_FOUND),
    DO_NOT_HAVE_ANY_DEPENDENCY(HttpStatus.BAD_REQUEST.value(), "Task do not have any dependency", HttpStatus.BAD_REQUEST),
    CIRCULAR_DEPENDENCY(HttpStatus.BAD_REQUEST.value(), "Circular dependency detected", HttpStatus.BAD_REQUEST),
    DEPENDENCIES_ALREADY_EXIST(HttpStatus.BAD_REQUEST.value(), "Dependencies already exist", HttpStatus.BAD_REQUEST),
    CAN_NOT_DELETE_TASK_WITH_DEPENDENCY(HttpStatus.BAD_REQUEST.value(), "Cannot delete task that has dependencies", HttpStatus.BAD_REQUEST),
    START_DATE_MUST_BE_BEFORE_END_DATE(HttpStatus.BAD_REQUEST.value(), "Start date must be before end date", HttpStatus.BAD_REQUEST),
    CAN_NOT_UPDATE_STATUS_OF_TASK_WITH_DEPENDENCY(HttpStatus.BAD_REQUEST.value(), "Cannot update status of task that has uncompleted dependencies", HttpStatus.BAD_REQUEST),
    CAN_NOT_SET_DEPENDENCY_FOR_NON_PENDING_TASK(HttpStatus.BAD_REQUEST.value(), "Cannot set dependency for a non-pending task", HttpStatus.BAD_REQUEST),
    DUE_DATE_MUST_BE_AFTER_NOW(HttpStatus.BAD_REQUEST.value(), "Due date must be after now", HttpStatus.BAD_REQUEST),

    // Task status change errors
    CAN_NOT_CHANGE_STATUS_OF_COMPLETED_TASK(HttpStatus.BAD_REQUEST.value(), "Cannot change status of completed task", HttpStatus.BAD_REQUEST),
    CAN_NOT_CHANGE_STATUS_OF_OVERDUE_TASK(HttpStatus.BAD_REQUEST.value(), "Cannot change status of overdue task, please extend due date", HttpStatus.BAD_REQUEST),
    CAN_NOT_ADD_OVERDUE_TASK_AS_DEPENDENCY(HttpStatus.BAD_REQUEST.value(), "Cannot add overdue task as dependency", HttpStatus.BAD_REQUEST),

    ;

    // Fields representing the error code, message, and HTTP status code
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
