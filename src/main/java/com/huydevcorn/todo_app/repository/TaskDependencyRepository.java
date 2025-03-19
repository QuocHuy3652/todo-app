package com.huydevcorn.todo_app.repository;

import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    Set<TaskDependency> findByTask(Task task);
    @Query("SELECT td.dependsOnTask.id FROM TaskDependency td WHERE td.task.id = :taskId")
    Set<Long> findDependsOnIdsByTaskId(Long taskId);
    Set<TaskDependency> findByTaskAndDependsOnTaskIdIn(Task task, Set<Long> dependsOnTaskIds);
    Set<TaskDependency> findAllByTask(Task task);
}
