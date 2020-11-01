package com.github.matjazmav.todo.service.webapi.entities;

import lombok.*;
import org.springframework.http.*;

import java.util.*;

@Value
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private List<String> errors;

    public static ResponseEntity<ApiResponse> ok() {
        return new ResponseEntity(
                new ApiResponse(null, null), HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return new ResponseEntity(
                new ApiResponse<>(data, null), HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, HttpStatus status) {
        return new ResponseEntity(
                new ApiResponse<>(data, null), status);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, HttpStatus status, String... errors) {
        return new ResponseEntity(
                new ApiResponse<>(data, Arrays.asList(errors)), status);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String... errors) {
        return new ResponseEntity(
                new ApiResponse<>(null, Arrays.asList(errors)), status);
    }
}
