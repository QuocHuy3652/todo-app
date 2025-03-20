package com.huydevcorn.todo_app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;

/**
 * Entity representing a dependency between tasks.
 * Ensures that a task cannot depend on itself.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "task_dependencies")
@Check(constraints = "task_id <> depends_on_task_id")
public class TaskDependency {
    // Unique identifier for the task dependency.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The task that has a dependency.
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // The task that the first task depends on.
    @ManyToOne
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOnTask;
}
