package com.huydevcorn.todo_app.service;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.request.TaskUpdateRequest;
import com.huydevcorn.todo_app.dto.response.PaginationResponse;
import com.huydevcorn.todo_app.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {
    public TaskResponse createTask(TaskCreationRequest request);
    public TaskResponse updateTask(Long id, TaskUpdateRequest request);
    public TaskResponse getTask(Long id);
    public void deleteTask(Long id);
    public TaskResponse changeStatus(Long id, String status);
    public PaginationResponse<TaskResponse> getTasks(int page, int size);

}
