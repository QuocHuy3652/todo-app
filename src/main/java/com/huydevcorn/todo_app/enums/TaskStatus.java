package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum TaskStatus {
    PENDING("PENDING", "Task is pending, this is the default status"),
    IN_PROGRESS("IN_PROGRESS", "Task is in progress"),
    DONE("DONE", "Task is done"),
    ;
    String status;
    String description;
}
