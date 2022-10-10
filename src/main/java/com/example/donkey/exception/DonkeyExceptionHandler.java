package com.example.donkey.exception;

import com.example.donkey.model.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class DonkeyExceptionHandler {
    public static final String VALIDATIONS_FAILED = "One or several validations failed";

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> onConstraintViolationException(ConstraintViolationException ex, WebRequest request){
         List<String> properties = ex.getConstraintViolations().stream()
                 .map(x -> x.getPropertyPath().toString())
                 .distinct()
                 .toList();


        Map<String, Object> errorMap = new HashMap<>();
         for(String property : properties){
             List<String> messages = new ArrayList<>();
             for(ConstraintViolation<?> violation : ex.getConstraintViolations()){
                 if(violation.getPropertyPath().toString().equals(property)){
                     messages.add(violation.getMessage());
                 }
             }
             errorMap.put(property, messages);
         }

         return ResponseEntity.badRequest().body(
                 new ExceptionResponse(
                         LocalDateTime.now(),
                         HttpStatus.BAD_REQUEST.value(),
                         HttpStatus.BAD_REQUEST.name(),
                         VALIDATIONS_FAILED,
                         request.getDescription(false),
                         errorMap
                 )
         );
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> onMultipartException(MultipartException multipartException, WebRequest request){
        return ResponseEntity.badRequest()
                .body(
                        new ExceptionResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.name(),
                                multipartException.getMessage(),
                                request.getDescription(false),
                                null
                        )
                );
    }

}
