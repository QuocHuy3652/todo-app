package com.huydevcorn.todo_app.dto.request;

import com.huydevcorn.todo_app.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class TaskCreationRequest {
    @NotNull(message = "TITLE_IS_REQUIRED")
    @NotEmpty(message = "TITLE_IS_REQUIRED")
    @NotBlank(message = "TITLE_IS_REQUIRED")
    String title;

    String description;
    LocalDateTime dueDate;
    String priority;
}
