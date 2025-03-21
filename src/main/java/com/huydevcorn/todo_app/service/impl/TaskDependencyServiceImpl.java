package com.huydevcorn.todo_app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huydevcorn.todo_app.dto.response.TaskDependencyResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
import com.huydevcorn.todo_app.enums.RedisPrefix;
import com.huydevcorn.todo_app.enums.TaskStatus;
import com.huydevcorn.todo_app.exception.AppException;
import com.huydevcorn.todo_app.exception.ErrorCode;
import com.huydevcorn.todo_app.repository.TaskDependencyRepository;
import com.huydevcorn.todo_app.repository.TaskRepository;
import com.huydevcorn.todo_app.service.RedisService;
import com.huydevcorn.todo_app.service.TaskDependencyService;
import com.huydevcorn.todo_app.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskDependencyService interface for managing task dependencies.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TaskDependencyServiceImpl implements TaskDependencyService {
    TaskRepository taskRepository;
    TaskDependencyRepository taskDependencyRepository;
    RedisService redisService;

    @Override
    public void addDependencies(Long taskId, Set<Long> dependentTaskIds) {
        // Validate that dependent task IDs are provided
        if (dependentTaskIds == null || dependentTaskIds.isEmpty()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_IDS_ARE_REQUIRED);
        }

        // Retrieve the task by ID and validate its status
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new AppException(ErrorCode.CAN_NOT_SET_DEPENDENCY_FOR_NON_PENDING_TASK);
        }

        // Ensure the task does not depend on itself
        if (dependentTaskIds.contains(taskId)) {
            throw new AppException(ErrorCode.TASK_CANNOT_DEPEND_ON_ITSELF);
        }

        // Retrieve dependent tasks and validate their existence
        Set<Task> dependentTasks = new HashSet<>(taskRepository.findAllById(dependentTaskIds));
        if (dependentTasks.size() != dependentTaskIds.size()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_NOT_FOUND);
        }

        // Validate the status of dependent tasks and check for circular dependencies
        for (Task dependentTask : dependentTasks) {
            if (dependentTask.getStatus() == TaskStatus.OVERDUE) {
                throw new AppException(ErrorCode.CAN_NOT_ADD_OVERDUE_TASK_AS_DEPENDENCY);
            }
            if (hasCircularDependency(task.getId(), dependentTask.getId())) {
                throw new AppException(ErrorCode.CIRCULAR_DEPENDENCY);
            }
        }

        // Retrieve existing dependencies to avoid duplicates
        Set<Long> existingDependenciesIds = taskDependencyRepository.findByTask(task).stream()
                .map(dep -> dep.getDependsOnTask().getId())
                .collect(Collectors.toSet());

        // Create new dependencies and save them
        Set<TaskDependency> newDependencies = dependentTasks.stream()
                .filter(dependentTask -> !existingDependenciesIds.contains(dependentTask.getId()))
                .map(dependentTask -> TaskDependency.builder()
                                                    .task(task)
                                                    .dependsOnTask(dependentTask)
                                                    .build())
                .collect(Collectors.toSet());

        // Save new dependencies if any, otherwise throw an exception
        if (!newDependencies.isEmpty()) {
            taskDependencyRepository.saveAll(newDependencies);

            // Delete cached dependencies
            redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.DEPENDENCIES.getPrefix(), "*"));
            redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.CHECK_CIRCLE.getPrefix(), "*"));
        } else {
            throw new AppException(ErrorCode.DEPENDENCIES_ALREADY_EXIST);
        }
    }

    @Override
    public TaskDependencyResponse getDependencies(Long taskId) {
        // Retrieve the task by ID
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Find all dependencies recursively
        Set<TaskDependencyResponse> allDependencies = new HashSet<>();
        findAllDependencies(task, allDependencies);

        // Build and return the task dependency response
        return TaskDependencyResponse.builder()
                .id(taskId)
                .title(task.getTitle())
                .dependsOnTasks(allDependencies)
                .build();
    }

    @Override
    public void removeDependency(Long taskId, Set<Long> dependentTaskIds) {
        // Validate that dependent task IDs are provided
        if (dependentTaskIds == null || dependentTaskIds.isEmpty()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_IDS_ARE_REQUIRED);
        }

        // Retrieve the task by ID
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Find and remove the specified dependencies
        Set<TaskDependency> dependenciesToRemove = taskDependencyRepository.findByTaskAndDependsOnTaskIdIn(task, dependentTaskIds);
        taskDependencyRepository.deleteAll(dependenciesToRemove);

        // Delete cached dependencies
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.DEPENDENCIES.getPrefix(), "*"));
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.CHECK_CIRCLE.getPrefix(), "*"));
    }

    @Override
    public void removeAllDependencies(Long taskId) {
        // Retrieve the task by ID
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        // Find and remove all dependencies
        Set<TaskDependency> dependenciesToRemove = taskDependencyRepository.findAllByTask(task);
        taskDependencyRepository.deleteAll(dependenciesToRemove);

        // Delete cached dependencies
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.DEPENDENCIES.getPrefix(), "*"));
        redisService.deleteByPattern(RedisUtils.withPrefix(RedisPrefix.CHECK_CIRCLE.getPrefix(), "*"));
    }


    // utility methods

    /**
     * Recursively finds all dependencies of a task.
     *
     * @param task the task to find dependencies for
     * @param dependencies the set to store found dependencies
     */
    private void findAllDependencies(
            Task task,
            Set<TaskDependencyResponse> dependencies
    ) {
        // Check if dependencies are already cached
        String key = RedisUtils.withPrefix(RedisPrefix.DEPENDENCIES.getPrefix(), task.getId().toString());
        if (redisService.hasKey(key)) {
            TypeReference<List<TaskDependencyResponse>> typeRef = new TypeReference<>() {};
            List<TaskDependencyResponse> cachedDependencies = redisService.getObject(key, typeRef);
            dependencies.addAll(new HashSet<>(cachedDependencies));
            return;
        }

        // Find direct dependencies of the task
        Set<TaskDependency> directDependencies = taskDependencyRepository.findByTask(task);

        // Recursively find dependencies for each direct dependency
        for (TaskDependency dependency : directDependencies) {
            Task dependentTask = dependency.getDependsOnTask();

            Set<TaskDependencyResponse> allDependencies = new HashSet<>();
            findAllDependencies(dependentTask, allDependencies);

            TaskDependencyResponse response = TaskDependencyResponse.builder()
                    .id(dependentTask.getId())
                    .title(dependentTask.getTitle())
                    .dependsOnTasks(allDependencies)
                    .build();

            dependencies.add(response);
        }

        // Cache the dependencies
        redisService.setObject(key, new ArrayList<>(dependencies),1, TimeUnit.HOURS);
    }

    /**
     * Checks for circular dependencies.
     *
     * @param taskId the ID of the task to check
     * @param newDependentTaskId the ID of the new dependent task
     * @return true if a circular dependency is found, false otherwise
     */
    private boolean hasCircularDependency(Long taskId, Long newDependentTaskId) {
        Set<Long> visited = new HashSet<>();
        Set<Long> stack = new HashSet<>();
        return dfsCheckCycle(newDependentTaskId, taskId, visited, stack);
    }

    /**
     * Depth-first search to check for cycles in the dependency graph.
     *
     * @param currentTaskId the current task ID
     * @param targetTaskId the target task ID
     * @param visited the set of visited task IDs
     * @param stack the set of task IDs in the current path
     * @return true if a cycle is found, false otherwise
     */
    private boolean dfsCheckCycle(Long currentTaskId, Long targetTaskId, Set<Long> visited, Set<Long> stack) {
        // Check if the current task is the target task
        if (currentTaskId.equals(targetTaskId)) {
            return true;
        }

        // Mark the current task as visited
        if (!visited.add(currentTaskId)) {
            return false;
        }

        // Add the current task to the stack
        stack.add(currentTaskId);

        // Cache key for dependencies of this task
        String key = RedisUtils.withPrefix(RedisPrefix.CHECK_CIRCLE.getPrefix(), currentTaskId.toString());

        Set<Long> dependencies;
        if (redisService.hasKey(key)) {
            // Get from cache
            TypeReference<List<Long>> typeRef = new TypeReference<>() {};
            dependencies = new HashSet<>(redisService.getObject(key, typeRef));
        } else {
            // Query database and cache result
            dependencies = taskDependencyRepository.findDependsOnIdsByTaskId(currentTaskId);
            redisService.setObject(key, new ArrayList<>(dependencies), 1, TimeUnit.HOURS);
        }

        // Recursively check for cycles in the dependencies
        for (Long dependentTaskId : dependencies) {
            if (dfsCheckCycle(dependentTaskId, targetTaskId, visited, stack)) {
                return true;
            }
        }

        // Remove the current task from the stack
        stack.remove(currentTaskId);
        return false;
    }
}
