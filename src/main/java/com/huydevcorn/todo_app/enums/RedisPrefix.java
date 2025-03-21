package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing Redis key prefixes and their descriptions.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RedisPrefix {

    // Prefix for individual task cache.
    TASK("task:", "Use for task cache"),

    // Prefix for task list cache.
    TASKS("tasks:", "Use for task list cache"),

    // Prefix for task dependency cache.
    DEPENDENCIES("dependencies:", "Use for task dependency cache"),

    // Prefix for check circle cache.
    CHECK_CIRCLE("check_circle:", "Use for check circle cache"),

    ;

    String prefix;
    String description;
}
