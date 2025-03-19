package com.huydevcorn.todo_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PaginationResponse<T> {
    int page;
    int perPage;
    int totalPages;
    long totalResults;
    List<T> results;
}
