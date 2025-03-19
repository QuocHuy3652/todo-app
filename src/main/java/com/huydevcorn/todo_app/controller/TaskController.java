package com.huydevcorn.todo_app.controller;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.ApiResponse;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/tasks")
@Tag(name = "Task", description = "Task API")
public class TaskController {
    TaskService taskService;

    @PostMapping
    @Operation(summary = "Create task")
    public ApiResponse<TaskResponse> createTask(@RequestBody @Valid TaskCreationRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.createTask(request))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.getTask(id))
                .build();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tasks")
    public ApiResponse<PaginationResponse<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PaginationResponse<TaskResponse>>builder()
                .data(taskService.getTasks(page, size))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ApiResponse<TaskResponse> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.updateTask(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status")
    public ApiResponse<TaskResponse> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.<TaskResponse>builder()
                .data(taskService.changeStatus(id, status))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ApiResponse<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.<String>builder()
                .message("Delete task successfully")
                .build();
    }
}
