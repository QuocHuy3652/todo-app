package com.huydevcorn.todo_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDependency extends JpaRepository<TaskDependency, Long> {
}
