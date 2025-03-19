package com.huydevcorn.todo_app.service.impl;

import com.huydevcorn.todo_app.dto.response.TaskDependencyResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
import com.huydevcorn.todo_app.exception.AppException;
import com.huydevcorn.todo_app.exception.ErrorCode;
import com.huydevcorn.todo_app.repository.TaskDependencyRepository;
import com.huydevcorn.todo_app.repository.TaskRepository;
import com.huydevcorn.todo_app.service.TaskDependencyService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TaskDependencyServiceImpl implements TaskDependencyService {
    TaskRepository taskRepository;
    TaskDependencyRepository taskDependencyRepository;

    @Override
    public void addDependencies(Long taskId, Set<Long> dependentTaskIds) {
        if (dependentTaskIds == null || dependentTaskIds.isEmpty()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_IDS_ARE_REQUIRED);
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (dependentTaskIds.contains(taskId)) {
            throw new AppException(ErrorCode.TASK_CANNOT_DEPEND_ON_ITSELF);
        }

        Set<Task> dependentTasks = new HashSet<>(taskRepository.findAllById(dependentTaskIds));

        if (dependentTasks.size() != dependentTaskIds.size()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_NOT_FOUND);
        }

        for (Task dependentTask : dependentTasks) {
            if (hasCircularDependency(task.getId(), dependentTask.getId())) {
                throw new AppException(ErrorCode.CIRCULAR_DEPENDENCY);
            }
        }

        Set<Long> existingDependenciesIds = taskDependencyRepository.findByTask(task).stream()
                .map(dep -> dep.getDependsOnTask().getId())
                .collect(Collectors.toSet());

        Set<TaskDependency> newDependencies = dependentTasks.stream()
                .filter(dependentTask -> !existingDependenciesIds.contains(dependentTask.getId()))
                .map(dependentTask -> TaskDependency.builder()
                                                    .task(task)
                                                    .dependsOnTask(dependentTask)
                                                    .build())
                .collect(Collectors.toSet());

        if (!newDependencies.isEmpty()) {
            taskDependencyRepository.saveAll(newDependencies);
        }
    }

    @Override
    public TaskDependencyResponse getDependencies(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Set<TaskDependencyResponse> allDependencies = new HashSet<>();
        findAllDependencies(task, allDependencies);

        return TaskDependencyResponse.builder()
                .id(taskId)
                .title(task.getTitle())
                .dependsOnTasks(allDependencies)
                .build();
    }

    @Override
    public void removeDependency(Long taskId, Set<Long> dependentTaskIds) {
        if (dependentTaskIds == null || dependentTaskIds.isEmpty()) {
            throw new AppException(ErrorCode.DEPENDENT_TASK_IDS_ARE_REQUIRED);
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        Set<TaskDependency> dependenciesToRemove = taskDependencyRepository.findByTaskAndDependsOnTaskIdIn(task, dependentTaskIds);
        taskDependencyRepository.deleteAll(dependenciesToRemove);
    }

    @Override
    public void removeAllDependencies(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        Set<TaskDependency> dependenciesToRemove = taskDependencyRepository.findAllByTask(task);
        taskDependencyRepository.deleteAll(dependenciesToRemove);
    }


    // utility methods
    private void findAllDependencies(
            Task task,
            Set<TaskDependencyResponse> dependencies
    ) {

        Set<TaskDependency> directDependencies = taskDependencyRepository.findByTask(task);

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
    }

    private boolean hasCircularDependency(Long taskId, Long newDependentTaskId) {
        Set<Long> visited = new HashSet<>();
        Set<Long> stack = new HashSet<>();
        return dfsCheckCycle(newDependentTaskId, taskId, visited, stack);
    }

    private boolean dfsCheckCycle(Long currentTaskId, Long targetTaskId, Set<Long> visited, Set<Long> stack) {
        if (currentTaskId.equals(targetTaskId)) {
            return true;
        }
        if (!visited.add(currentTaskId)) {
            return false;
        }

        stack.add(currentTaskId);

        Set<Long> dependencies = taskDependencyRepository.findDependsOnIdsByTaskId(currentTaskId);

        for (Long dependentTaskId : dependencies) {
            if (dfsCheckCycle(dependentTaskId, targetTaskId, visited, stack)) {
                return true;
            }
        }

        stack.remove(currentTaskId);
        return false;
    }


}
