package com.huydevcorn.todo_app.repository;

import com.huydevcorn.todo_app.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing tasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    // Additional query methods can be defined here if needed
}
