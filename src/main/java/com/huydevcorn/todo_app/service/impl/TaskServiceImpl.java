package com.huydevcorn.todo_app.service.impl;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
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
        TaskPriority priority = TaskPriority.MEDIUM;
        if (request.getPriority() != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(request.getPriority()));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
            priority = TaskPriority.valueOf(request.getPriority().toUpperCase());
        }
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DUE_DATE_MUST_BE_AFTER_NOW);
        }
        Task newTask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();
        newTask.setStatus(TaskStatus.PENDING);
        newTask.setPriority(priority);
        newTask = taskRepository.save(newTask);

        notificationScheduler.scheduleTask(
                newTask.getId(),
                newTask.getTitle(),
                newTask.getDueDate()
        );

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
            if (request.getDueDate().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.DUE_DATE_MUST_BE_AFTER_NOW);
            }
            updateTask.setDueDate(request.getDueDate());
        }

        updateTask = taskRepository.save(updateTask);

        notificationScheduler.cancelTask(id);
        notificationScheduler.scheduleTask(
                updateTask.getId(),
                updateTask.getTitle(),
                updateTask.getDueDate()
        );

        return taskMapper.toTaskResponse(updateTask);
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        redisService.setValue("task:" + id, "test redis connection");
        return taskMapper.toTaskResponse(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        if (taskDependencyRepository.existsByTaskId(id) || taskDependencyRepository.existsByDependsOnTaskId(id)) {
            throw new AppException(ErrorCode.CAN_NOT_DELETE_TASK_WITH_DEPENDENCY);
        }
        taskRepository.delete(task);
        notificationScheduler.cancelTask(id);
    }

    @Override
    public TaskResponse changeStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        Set<TaskDependency> taskDependency = taskDependencyRepository.findByTask(task);
        taskDependency.forEach(td -> {
            if (td.getDependsOnTask().getStatus() != TaskStatus.DONE) {
                throw new AppException(ErrorCode.CAN_NOT_UPDATE_STATUS_OF_TASK_WITH_DEPENDENCY);
            }
        });
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
    public PaginationResponse<TaskResponse> getTasks(
            int page,
            int size,
            String title,
            String priority,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {
        if (page < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE);
        }
        if (size < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (priority != null) {
            boolean isValid = Arrays.stream(TaskPriority.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(priority));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_PRIORITY);
            }
        }
        if (status != null) {
            boolean isValid = Arrays.stream(TaskStatus.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(status));
            if (!isValid) {
                throw new AppException(ErrorCode.INVALID_STATUS);
            }
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.START_DATE_MUST_BE_BEFORE_END_DATE);
        }

        Specification<Task> spec = Specification
                .where(TaskSpecification.filterByTitleOrDescription(title))
                .and(TaskSpecification.filterByPriority(priority == null ? null : priority.toUpperCase()))
                .and(TaskSpecification.filterByDueDateRange(startDate, endDate))
                .and(TaskSpecification.filterByStatus(status == null ? null : status.toUpperCase()));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return PaginationResponse.<TaskResponse>builder()
                .page(page)
                .perPage(size)
                .totalPages(taskPage.getTotalPages())
                .totalResults(taskPage.getTotalElements())
                .results(taskPage.map(taskMapper::toTaskResponse).getContent())
                .build();
    }
}
