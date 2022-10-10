package com.example.donkey.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ExceptionResponse(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timeStamp,
        Integer status,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, Object> errors
) {
}


