package com.huydevcorn.todo_app.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum TaskPriority {
    LOW(1, "Low, not important"),
    MEDIUM(2, "Medium, normal, this is the default priority"),
    HIGH(3, "High, important"),
    URGENT(4, "Urgent, very important"),
    CRITICAL(5, "Critical, extremely important, must be done immediately"),
    ;
    int value;
    String description;
}
