package com.huydevcorn.todo_app.repository;

import com.huydevcorn.todo_app.entity.Task;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findTaskById(Long id);

    @Nonnull
    Page<Task> findAll(@Nonnull Pageable pageable);
}
