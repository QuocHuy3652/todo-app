package com.huydevcorn.todo_app.service;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service interface for managing tasks.
 */
public interface TaskService {
    /**
     * Creates a new task.
     *
     * @param request the task creation request
     * @return the created task response
     */
    TaskResponse createTask(TaskCreationRequest request);

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param request the task update request
     * @return the updated task response
     */
    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    /**
     * Retrieves a task by its ID.
     *
     * @param id the task ID
     * @return the task response
     */
    TaskResponse getTask(Long id);

    /**
     * Deletes a task by its ID.
     *
     * @param id the task ID
     */
    void deleteTask(Long id);

    /**
     * Changes the status of a task.
     *
     * @param id the task ID
     * @param status the new status
     * @return the updated task response
     */
    TaskResponse changeStatus(Long id, String status);

    /**
     * Extends the due date of a task.
     *
     * @param id the task ID
     * @param dueDate the new due date
     * @return the updated task response
     */
    TaskResponse extendDueDate(Long id, LocalDateTime dueDate);

    /**
     * Retrieves a paginated list of tasks with optional filters.
     *
     * @param page the page number
     * @param size the page size
     * @param title the task title filter
     * @param priority the task priority filter
     * @param startDate the start date filter
     * @param endDate the end date filter
     * @param status the task status filter
     * @return the paginated task response
     */
    PaginationResponse<TaskResponse> getTasks(int page, int size, String title, String priority, LocalDate startDate, LocalDate endDate, String status);

}
