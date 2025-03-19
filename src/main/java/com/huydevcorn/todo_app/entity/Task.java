package com.huydevcorn.todo_app.entity;

import com.huydevcorn.todo_app.enums.TaskPriority;
import com.huydevcorn.todo_app.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(nullable = false)
    LocalDateTime dueDate;

    @Column(nullable = false)
    int priority = TaskPriority.MEDIUM.getValue();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskStatus status = TaskStatus.PENDING;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
