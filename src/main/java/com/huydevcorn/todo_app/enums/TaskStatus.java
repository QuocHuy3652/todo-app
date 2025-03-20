package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing task statuses with their values and descriptions.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum TaskStatus {
    // Task is pending, this is the default status.
    PENDING("PENDING", "Task is pending, this is the default status"),

    // Task is in progress.
    IN_PROGRESS("IN_PROGRESS", "Task is in progress"),

    // Task is done.
    DONE("DONE", "Task is done"),

    // Task is overdue.
    OVERDUE("OVERDUE", "Task is overdue"),

    ;

    String status;
    String description;
}
