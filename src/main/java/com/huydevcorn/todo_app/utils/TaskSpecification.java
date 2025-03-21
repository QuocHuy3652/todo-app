package com.huydevcorn.todo_app.utils;

import com.huydevcorn.todo_app.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Utility class for creating JPA Specifications for filtering Task entities.
 */
public class TaskSpecification {
    /**
     * Creates a specification to filter tasks by title or description.
     *
     * @param keyword the keyword to search in title or description
     * @return a specification for filtering tasks by title or description
     */
    public static Specification<Task> filterByTitleOrDescription(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), pattern),
                    criteriaBuilder.like(root.get("description"), pattern)
            );
        };
    }

    /**
     * Creates a specification to filter tasks by priority.
     *
     * @param priority the priority to filter tasks by
     * @return a specification for filtering tasks by priority
     */
    public static Specification<Task> filterByPriority(String priority) {
        return (root, query, criteriaBuilder) ->
                priority == null ? null : criteriaBuilder.equal(root.get("priority"), priority);
    }

    /**
     * Creates a specification to filter tasks by due date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a specification for filtering tasks by due date range
     */
    public static Specification<Task> filterByDueDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("dueDate"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDate);
        };
    }

    /**
     * Creates a specification to filter tasks by status.
     *
     * @param status the status to filter tasks by
     * @return a specification for filtering tasks by status
     */
    public static Specification<Task> filterByStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }
}
