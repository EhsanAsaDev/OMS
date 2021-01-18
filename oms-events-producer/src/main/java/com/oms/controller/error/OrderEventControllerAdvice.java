package com.oms.controller.error;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class OrderEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleRequestBody(MethodArgumentNotValidException ex) {
        //generate somethings like : order.id - must not be null, order.type - must not be null
        List<FieldError> errorList = ex.getBindingResult().getFieldErrors();
        String errorMessages=errorList.stream()
                .map(error -> error.getField() + " - " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        log.info("errorMessage : {} ", errorMessages);
        return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
    }
}
