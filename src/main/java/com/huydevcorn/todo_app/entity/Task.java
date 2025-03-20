package com.huydevcorn.todo_app.entity;

import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "due_date")
    LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskStatus status = TaskStatus.PENDING;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;

    @Column(name = "update_at", insertable = false)
    private LocalDateTime updatedAt = null;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
