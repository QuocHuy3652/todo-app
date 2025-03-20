package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum TaskPriority {
    LOW(1, "Low", "Not important"),
    MEDIUM(2, "Medium", "Normal, this is the default priority"),
    HIGH(3, "High" , "Important"),
    URGENT(4, "Urgent", "Very important"),
    CRITICAL(5, "Critical", "Extremely important, must be done immediately"),
    ;
    int value;
    String priority;
    String description;
}
