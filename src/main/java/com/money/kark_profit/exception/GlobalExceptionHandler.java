package com.money.kark_profit.exception;

import com.money.kark_profit.utils.ResponseBuilderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ResponseBuilderUtils<Void>> handleSystemException(SystemException ex) {
        ResponseBuilderUtils<Void> response = new ResponseBuilderUtils<>(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ResponseBuilderUtils<Void>> handleDatabaseException(DatabaseException ex) {
        ResponseBuilderUtils<Void> response = new ResponseBuilderUtils<>(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBuilderUtils<Void>> handleOtherExceptions(Exception ex) {
        ResponseBuilderUtils<Void> response = new ResponseBuilderUtils<>("UNKNOWN_ERROR", ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}