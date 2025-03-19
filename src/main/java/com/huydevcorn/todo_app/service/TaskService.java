package com.huydevcorn.todo_app.service;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;

public interface TaskService {
    TaskResponse createTask(TaskCreationRequest request);
    TaskResponse updateTask(Long id, TaskUpdateRequest request);
    TaskResponse getTask(Long id);
    void deleteTask(Long id);
    TaskResponse changeStatus(Long id, String status);
    PaginationResponse<TaskResponse> getTasks(int page, int size);

}
