package com.huydevcorn.todo_app.service;

import com.huydevcorn.todo_app.dto.response.TaskDependencyResponse;

import java.util.Set;

/**
 * Service interface for managing task dependencies.
 */
public interface TaskDependencyService {
    /**
     * Adds dependencies to a task.
     *
     * @param taskId the ID of the task to add dependencies to
     * @param dependentTaskIds the IDs of the tasks to be added as dependencies
     */
    void addDependencies(Long taskId, Set<Long> dependentTaskIds);

    /**
     * Retrieves the dependencies of a task.
     *
     * @param taskId the ID of the task to retrieve dependencies for
     * @return the task dependency response
     */
    TaskDependencyResponse getDependencies(Long taskId);

    /**
     * Removes specific dependencies from a task.
     *
     * @param taskId the ID of the task to remove dependencies from
     * @param dependentTaskIds the IDs of the tasks to be removed as dependencies
     */
    void removeDependency(Long taskId, Set<Long> dependentTaskIds);

    /**
     * Removes all dependencies from a task.
     *
     * @param taskId the ID of the task to remove all dependencies from
     */
    void removeAllDependencies(Long taskId);
}
