package com.huydevcorn.todo_app.repository;

import com.huydevcorn.todo_app.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
}
