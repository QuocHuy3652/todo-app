package com.huydevcorn.todo_app.controller;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.ApiResponse;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controller for handling task - related operations.
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/tasks")
@Tag(name = "Task", description = "Task API")
public class TaskController {
    /**
     * Service for task-related operations.
     */
    TaskService taskService;

    /**
     * Endpoint to create a new task.
     *
     * @param request the task creation request
     * @return the created task response
     */
    @PostMapping
    @Operation(summary = "Create task")
    public ApiResponse<TaskResponse> createTask(@RequestBody @Valid TaskCreationRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.createTask(request))
                .build();
    }

    /**
     * Endpoint to get a task by its ID.
     *
     * @param id the task ID
     * @return the task response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.getTask(id))
                .build();
    }

    /**
     * Endpoint to get all tasks with optional filters.
     *
     * @param page the page number
     * @param size the page size
     * @param title the task title filter
     * @param priority the task priority filter
     * @param startDate the start date filter
     * @param endDate the end date filter
     * @param status the task status filter
     * @return the paginated task response
     */
    @GetMapping("/all")
    @Operation(summary = "Get all tasks")
    public ApiResponse<PaginationResponse<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false)
            @Parameter(
                    schema = @Schema(
                            example = "'LOW', 'MEDIUM', 'HIGH', 'URGENT', or 'CRITICAL'"
                    )
            )
            String priority,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    schema = @Schema(
                            example = "2025-03-20"
                    )
            )
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    schema = @Schema(
                            example = "2025-03-20"
                    )
            )
            LocalDate endDate,
            @RequestParam(required = false)
            @Parameter(
                    schema = @Schema(
                            example = "'PENDING', 'IN_PROGRESS', or 'DONE'"
                    )
            )
            String status
    ) {
        return ApiResponse.<PaginationResponse<TaskResponse>>builder()
                .data(taskService.getTasks(page, size, title, priority, startDate, endDate, status))
                .build();
    }

    /**
     * Endpoint to update a task.
     *
     * @param id the task ID
     * @param request the task update request
     * @return the updated task response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ApiResponse<TaskResponse> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.updateTask(id, request))
                .build();
    }

    /**
     * Endpoint to change the status of a task.
     *
     * @param id the task ID
     * @param status the new status
     * @return the updated task response
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status")
    public ApiResponse<TaskResponse> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.changeStatus(id, status))
                .build();
    }

    /**
     * Endpoint to extend the due date of a task.
     *
     * @param id the task ID
     * @param dueDate the new due date
     * @return the updated task response
     */
    @PatchMapping("/{id}/due-date")
    @Operation(summary = "Extend due date")
    public ApiResponse<TaskResponse> extendDueDate(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    schema = @Schema(
                            example = "2025-03-20T18:55:16.353Z"
                    )
            )
            LocalDateTime dueDate
    ) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.extendDueDate(id, dueDate))
                .build();
    }

    /**
     * Endpoint to delete a task.
     *
     * @param id the task ID
     * @return a message indicating the result
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ApiResponse<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.<String>builder()
                .message("Delete task successfully")
                .build();
    }
}
