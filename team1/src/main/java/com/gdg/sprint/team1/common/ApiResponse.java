package com.gdg.sprint.team1.common;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    ErrorDetail error,
    String timestamp
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, Instant.now().toString());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(
            false,
            null,
            null,
            new ErrorDetail(code, message, null),
            Instant.now().toString()
        );
    }

    public static <T> ApiResponse<T> failure(String code, String message, List<FieldErrorEntry> fieldErrors) {
        return new ApiResponse<>(
            false,
            null,
            null,
            new ErrorDetail(code, message, fieldErrors),
            Instant.now().toString()
        );
    }

    public record ErrorDetail(String code, String message, List<FieldErrorEntry> fieldErrors) {

        public ErrorDetail(String code, String message) {
            this(code, message, null);
        }
    }

    public record FieldErrorEntry(String field, String message) {}
}
