package com.huydevcorn.todo_app.mapper;

import com.huydevcorn.todo_app.dto.request.TaskCreationRequest;
import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.entity.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    Task toTask(TaskCreationRequest request);
    TaskResponse toTaskResponse(Task task);
}
