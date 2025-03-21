package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing task priorities with their values, names, and descriptions.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum TaskPriority {
    // Low priority task.
    LOW(1, "Low", "Not important"),

    // Medium priority task. This is the default priority.
    MEDIUM(2, "Medium", "Normal, this is the default priority"),

    // High priority task.
    HIGH(3, "High" , "Important"),

    // Urgent priority task.
    URGENT(4, "Urgent", "Very important"),

    // Critical priority task. Must be done immediately.
    CRITICAL(5, "Critical", "Extremely important, must be done immediately"),

    ;

    int value;
    String priority;
    String description;
}
