package com.huydevcorn.todo_app.entity;

import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a task with various attributes such as title, description, due date, priority, status, etc.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tasks")
public class Task {
    // Unique identifier for the task.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Title of the task. This field is mandatory.
    @Column(nullable = false)
    String title;

    // Description of the task.
    @Column(columnDefinition = "TEXT")
    String description;

    // Due date of the task.
    @Column(name = "due_date")
    LocalDateTime dueDate;

    // Priority of the task. Default is MEDIUM.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskPriority priority = TaskPriority.MEDIUM;

    // Status of the task. Default is PENDING.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskStatus status = TaskStatus.PENDING;

    // Timestamp when the task was created. This field is automatically populated.
    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;

    // Timestamp when the task was last updated. This field is automatically populated.
    @Column(name = "update_at", insertable = false)
    private LocalDateTime updatedAt = null;

    // Method to update the `updatedAt` field with the current timestamp before updating the entity.
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
