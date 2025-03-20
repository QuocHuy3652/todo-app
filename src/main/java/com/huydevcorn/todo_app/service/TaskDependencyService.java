package com.huydevcorn.todo_app.service;

import com.huydevcorn.todo_app.dto.response.TaskDependencyResponse;

import java.util.Set;

public interface TaskDependencyService {
    void addDependencies(Long taskId, Set<Long> dependentTaskIds);
    TaskDependencyResponse getDependencies(Long taskId);
    void removeDependency(Long taskId, Set<Long> dependentTaskIds);
    void removeAllDependencies(Long taskId);
}
