package com.huydevcorn.todo_app.controller;

import com.huydevcorn.todo_app.dto.response.ApiResponse;
import com.huydevcorn.todo_app.dto.response.TaskDependencyResponse;
import com.huydevcorn.todo_app.service.TaskDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller for handling task dependency - related operations.
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/dependencies")
@Tag(name = "Task dependencies", description = "Task dependencies API")
public class TaskDependencyController {
    /**
     * Service for task dependency-related operations.
     */
    TaskDependencyService taskDependencyService;

    /**
     * Endpoint to create dependencies for a task.
     *
     * @param taskId the ID of the task
     * @param dependentTaskId the IDs of the tasks that the task depends on
     * @return a message indicating the result
     */
    @PostMapping("/{taskId}")
    @Operation(summary = "Create dependencies")
    public ApiResponse<String> createDependency(
            @PathVariable Long taskId,
            @RequestParam Set<Long> dependentTaskId
    ) {
        taskDependencyService.addDependencies(taskId, dependentTaskId);
        return ApiResponse.<String>builder()
                .message("Dependencies created successfully")
                .build();
    }

    /**
     * Endpoint to get the dependencies of a task.
     *
     * @param taskId the ID of the task
     * @return the task dependency response
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "Get dependencies of a task")
    public ApiResponse<TaskDependencyResponse> getDependency(
            @PathVariable Long taskId
    ) {
        return ApiResponse.<TaskDependencyResponse>builder()
                .data(taskDependencyService.getDependencies(taskId))
                .build();
    }

    /**
     * Endpoint to delete some dependencies of a task.
     *
     * @param taskId the ID of the task
     * @param dependentTaskId the IDs of the tasks to remove from dependencies
     * @return a message indicating the result
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete some dependencies")
    public ApiResponse<String> deleteDependency(
            @PathVariable Long taskId,
            @RequestParam Set<Long> dependentTaskId
    ) {
        taskDependencyService.removeDependency(taskId, dependentTaskId);
        return ApiResponse.<String>builder()
                .message("Dependency deleted successfully")
                .build();
    }

    /**
     * Endpoint to delete all dependencies of a task.
     *
     * @param taskId the ID of the task
     * @return a message indicating the result
     */
    @DeleteMapping("/all/{taskId}")
    @Operation(summary = "Delete all dependencies")
    public ApiResponse<String> deleteAllDependencies(@PathVariable Long taskId) {
        taskDependencyService.removeAllDependencies(taskId);
        return ApiResponse.<String>builder()
                .message("All dependencies deleted successfully")
                .build();
    }
}
