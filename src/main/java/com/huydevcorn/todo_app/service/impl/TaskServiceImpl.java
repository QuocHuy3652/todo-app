package com.huydevcorn.todo_app.service.impl;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import com.huydevcorn.todo_app.exception.AppException;
import com.huydevcorn.todo_app.exception.ErrorCode;
import com.huydevcorn.todo_app.mapper.TaskMapper;
import com.huydevcorn.todo_app.repository.TaskRepository;
import com.huydevcorn.todo_app.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {
    TaskRepository taskRepository;
    TaskMapper taskMapper;

    @Override
    public TaskResponse createTask(TaskCreationRequest request) {
        TaskPriority priority = TaskPriority.MEDIUM;
        if (request.getPriority() != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(request.getPriority()));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
            priority = TaskPriority.valueOf(request.getPriority().toUpperCase());
        }
        Task newTask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();
        newTask.setStatus(TaskStatus.PENDING);
        newTask.setPriority(priority);
        newTask = taskRepository.save(newTask);
        return taskMapper.toTaskResponse(newTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        if (request.getPriority() != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(request.getPriority()));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
        }
        Task updateTask = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (request.getTitle() != null) {
            updateTask.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            updateTask.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            updateTask.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
        }
        if (request.getDueDate() != null) {
            updateTask.setDueDate(request.getDueDate());
        }

        updateTask = taskRepository.save(updateTask);
        return taskMapper.toTaskResponse(updateTask);
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        return taskMapper.toTaskResponse(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse changeStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        try {
            TaskStatus newStatus = TaskStatus.valueOf(status.toUpperCase());
            task.setStatus(newStatus);
            task = taskRepository.save(task);
            return taskMapper.toTaskResponse(task);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
    }

    @Override
    public PaginationResponse<TaskResponse> getTasks(int page, int size) {
        if (page < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE);
        }
        if (size < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.findAll(pageable);

        return PaginationResponse.<TaskResponse>builder()
                .page(page)
                .perPage(size)
                .totalPages(taskPage.getTotalPages())
                .totalResults(taskPage.getTotalElements())
                .results(taskPage.map(taskMapper::toTaskResponse).getContent())
                .build();
    }
}
