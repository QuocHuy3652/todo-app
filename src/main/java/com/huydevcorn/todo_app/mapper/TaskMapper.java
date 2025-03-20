package com.huydevcorn.todo_app.mapper;

import com.huydevcorn.todo_app.dto.response.TaskResponse;
import com.huydevcorn.todo_app.entity.Task;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Task entities to TaskResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {
    /**
     * Converts a Task entity to a TaskResponse DTO.
     *
     * @param task the Task entity to convert
     * @return the converted TaskResponse DTO
     */
    TaskResponse toTaskResponse(Task task);
}
