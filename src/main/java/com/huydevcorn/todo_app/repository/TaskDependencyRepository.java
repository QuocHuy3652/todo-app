package com.huydevcorn.todo_app.repository;

import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Repository interface for managing task dependencies.
 */
@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    /**
     * Finds all dependencies for a given task.
     *
     * @param task the task to find dependencies for
     * @return a set of task dependencies
     */
    Set<TaskDependency> findByTask(Task task);

    /**
     * Finds the IDs of tasks that a given task depends on.
     *
     * @param taskId the ID of the task to find dependencies for
     * @return a set of task IDs that the task depends on
     */
    @Query("SELECT td.dependsOnTask.id FROM TaskDependency td WHERE td.task.id = :taskId")
    Set<Long> findDependsOnIdsByTaskId(Long taskId);

    /**
     * Finds specific dependencies for a given task.
     *
     * @param task the task to find dependencies for
     * @param dependsOnTaskIds the IDs of the tasks to find as dependencies
     * @return a set of task dependencies
     */
    Set<TaskDependency> findByTaskAndDependsOnTaskIdIn(Task task, Set<Long> dependsOnTaskIds);

    /**
     * Finds all dependencies for a given task.
     *
     * @param task the task to find dependencies for
     * @return a set of task dependencies
     */
    Set<TaskDependency> findAllByTask(Task task);

    /**
     * Checks if a task has any dependencies.
     *
     * @param taskId the ID of the task to check
     * @return true if the task has dependencies, false otherwise
     */
    boolean existsByTaskId(Long taskId);

    /**
     * Checks if a task is a dependency for any other task.
     *
     * @param dependsOnTaskId the ID of the task to check
     * @return true if the task is a dependency, false otherwise
     */
    boolean existsByDependsOnTaskId(Long dependsOnTaskId);
}
