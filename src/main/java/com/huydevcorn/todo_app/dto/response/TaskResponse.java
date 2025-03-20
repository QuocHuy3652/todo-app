package com.huydevcorn.todo_app.dto.response;

import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TaskResponse {
    Long id;
    String title;
    String description;
    TaskPriority priority;
    TaskStatus status;
    LocalDateTime dueDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
