package com.huydevcorn.todo_app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
import com.huydevcorn.todo_app.enums.RedisPrefix;
import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import com.huydevcorn.todo_app.exception.AppException;
import com.huydevcorn.todo_app.exception.ErrorCode;
import com.huydevcorn.todo_app.mapper.TaskMapper;
import com.huydevcorn.todo_app.notification.NotificationScheduler;
import com.huydevcorn.todo_app.repository.TaskDependencyRepository;
import com.huydevcorn.todo_app.repository.TaskRepository;
import com.huydevcorn.todo_app.service.RedisService;
import com.huydevcorn.todo_app.service.TaskService;
import com.huydevcorn.todo_app.utils.RedisUtils;
import com.huydevcorn.todo_app.utils.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the TaskService interface for managing tasks.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {
    TaskRepository taskRepository;
    TaskMapper taskMapper;
    TaskDependencyRepository taskDependencyRepository;
    NotificationScheduler notificationScheduler;
    RedisService redisService;

    @Override
    public TaskResponse createTask(TaskCreationRequest request) {
        // Validate and set task priority
        TaskPriority priority = TaskPriority.MEDIUM;
        if (request.getPriority() != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(request.getPriority()));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
            priority = TaskPriority.valueOf(request.getPriority().toUpperCase());
        }

        // Validate due date
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DUE_DATE_MUST_BE_AFTER_NOW);
        }

        // Create and save new task
        Task newTask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();
        newTask.setStatus(TaskStatus.PENDING);
        newTask.setPriority(priority);
        newTask = taskRepository.save(newTask);

        // Schedule notification for the new task
        if (newTask.getDueDate() != null) {
            notificationScheduler.scheduleTask(
                    newTask.getId(),
                    newTask.getTitle(),
                    newTask.getDueDate()
            );
        }

        // Clear related cache
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.TASKS.getPrefix(), "*"));

        return taskMapper.toTaskResponse(newTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        // Validate priority
        if (request.getPriority() != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(request.getPriority()));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
        }

        // Find and update task
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
            if (request.getDueDate().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.DUE_DATE_MUST_BE_AFTER_NOW);
            }
            updateTask.setDueDate(request.getDueDate());
        }

        updateTask = taskRepository.save(updateTask);

        // Reschedule notification for the updated task
        notificationScheduler.cancelTask(id);
        if (updateTask.getDueDate() != null) {
            notificationScheduler.scheduleTask(
                    updateTask.getId(),
                    updateTask.getTitle(),
                    updateTask.getDueDate()
            );
        }

        // Clear related cache
        redisService.delete(RedisUtils.withPrefix(RedisPrefix.TASK.getPrefix(), id.toString()));
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.TASKS.getPrefix(), "*"));

        return taskMapper.toTaskResponse(updateTask);
    }

    @Override
    public TaskResponse getTask(Long id) {
        // Check cache for task
        String key = RedisUtils.withPrefix(RedisPrefix.TASK.getPrefix(), id.toString());
        TypeReference<TaskResponse> typeRef = new TypeReference<>() {};
        TaskResponse cachedTask = redisService.getObject(key, typeRef);
        if (cachedTask != null) {
            return cachedTask;
        }

        // Fetch task from repository
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        TaskResponse response = taskMapper.toTaskResponse(task);

        // Cache the task response
        redisService.setObject(key, response, 5, TimeUnit.MINUTES);

        return response;
    }

    @Override
    public void deleteTask(Long id) {
        // Find task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Check for dependencies
        if (taskDependencyRepository.existsByTaskId(id) || taskDependencyRepository.existsByDependsOnTaskId(id)) {
            throw new AppException(ErrorCode.CAN_NOT_DELETE_TASK_WITH_DEPENDENCY);
        }

        // Delete task and cancel notification
        taskRepository.delete(task);
        notificationScheduler.cancelTask(id);

        // Clear related cache
        redisService.delete(RedisUtils.withPrefix(RedisPrefix.TASK.getPrefix(), id.toString()));
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.TASKS.getPrefix(), "*"));
    }

    @Override
    public TaskResponse changeStatus(Long id, String status) {
        // Find task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Validate status change
        if (task.getStatus() == TaskStatus.DONE) {
            throw new AppException(ErrorCode.CAN_NOT_CHANGE_STATUS_OF_COMPLETED_TASK);
        } else if (task.getStatus() == TaskStatus.OVERDUE) {
            throw new AppException(ErrorCode.CAN_NOT_CHANGE_STATUS_OF_OVERDUE_TASK);
        }

        // Check dependencies
        Set<TaskDependency> taskDependency = taskDependencyRepository.findByTask(task);
        taskDependency.forEach(td -> {
            if (td.getDependsOnTask().getStatus() != TaskStatus.DONE) {
                throw new AppException(ErrorCode.CAN_NOT_UPDATE_STATUS_OF_TASK_WITH_DEPENDENCY);
            }
        });

        // Update status
        try {
            TaskStatus newStatus = TaskStatus.valueOf(status.toUpperCase());
            task.setStatus(newStatus);
            task = taskRepository.save(task);

            // Cancel notification if task is done
            if (newStatus == TaskStatus.DONE) {
                notificationScheduler.cancelTask(id);
            }

            // Clear related cache
            redisService.delete(RedisUtils.withPrefix(RedisPrefix.TASK.getPrefix(), id.toString()));
            redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.TASKS.getPrefix(), "*"));

            return taskMapper.toTaskResponse(task);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
    }

    @Override
    public TaskResponse extendDueDate(Long id, LocalDateTime dueDate) {
        // Find task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Validate due date
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DUE_DATE_MUST_BE_AFTER_NOW);
        }

        // Update due date and status
        task.setDueDate(dueDate);
        task.setStatus(TaskStatus.PENDING);
        task = taskRepository.save(task);

        // Reschedule notification
        if (task.getDueDate() != null) {
            notificationScheduler.scheduleTask(
                    task.getId(),
                    task.getTitle(),
                    task.getDueDate()
            );
        }

        // Clear related cache
        redisService.delete(RedisUtils.withPrefix(RedisPrefix.TASK.getPrefix(), id.toString()));
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.TASKS.getPrefix(), "*"));

        return taskMapper.toTaskResponse(task);
    }

    @Override
    public PaginationResponse<TaskResponse> getTasks(
            int page,
            int size,
            String title,
            String priority,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {
        // Validate pagination parameters
        if (page < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE);
        }
        if (size < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        // Validate priority
        if (priority != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(priority));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
        }

        // Validate status
        if (status != null) {
            boolean isValid = Arrays.stream(TaskStatus.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(status));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_STATUS);
            }
        }

        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.START_DATE_MUST_BE_BEFORE_END_DATE);
        }

        // Check cache for tasks
        String key = RedisUtils.withPrefix(
                RedisPrefix.TASKS.getPrefix(),
                String.format("page=%d:size=%d:title=%s:priority=%s:start=%s:end=%s:status=%s",
                        page, size,
                        title != null ? title : "all",
                        priority != null ? priority : "all",
                        startDate != null ? startDate.toString() : "all",
                        endDate != null ? endDate.toString() : "all",
                        status != null ? status : "all"
                )
        );

        TypeReference<PaginationResponse<TaskResponse>> typeRef = new TypeReference<>() {};
        PaginationResponse<TaskResponse> cachedResponse = redisService.getObject(key, typeRef);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Build specification for filtering tasks
        Specification<Task> spec = Specification
                .where(TaskSpecification.filterByTitleOrDescription(title))
                .and(TaskSpecification.filterByPriority(priority == null ? null : priority.toUpperCase()))
                .and(TaskSpecification.filterByDueDateRange(startDate, endDate))
                .and(TaskSpecification.filterByStatus(status == null ? null : status.toUpperCase()));

        // Fetch tasks from repository
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        // Build pagination response
        PaginationResponse<TaskResponse> response = PaginationResponse.<TaskResponse>builder()
                .page(page)
                .perPage(size)
                .totalPages(taskPage.getTotalPages())
                .totalResults(taskPage.getTotalElements())
                .results(taskPage.map(taskMapper::toTaskResponse).getContent())
                .build();

        // Cache the response
        redisService.setObject(key, response, 5, TimeUnit.MINUTES);

        return response;
    }
}
