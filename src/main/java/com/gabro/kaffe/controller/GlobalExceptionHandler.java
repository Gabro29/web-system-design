package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.error.ApiErrorDTO;
import com.gabro.kaffe.exception.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;



@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private ResponseEntity<ApiErrorDTO> buildResponse(HttpStatus status, String message) {
        ApiErrorDTO error = new ApiErrorDTO(
                String.valueOf(status.value()),
                message
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        return new ResponseEntity<>(error, headers, status);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    @ExceptionHandler(CustomLogicException.class)
    public ResponseEntity<ApiErrorDTO> handleBusinessLogic(CustomLogicException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    @ExceptionHandler(MacchinettaOutServiceException.class)
    public ResponseEntity<ApiErrorDTO> handleMacchinettaGuasta(MacchinettaOutServiceException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Si è verificato un errore imprevisto");
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorDTO> handleBadRequestFrameworkExceptions(Exception ex) {
        String userMessage = "Richiesta non valida";

        if (ex instanceof MethodArgumentTypeMismatchException typeEx) {
            userMessage = "Valore non valido per il parametro: " + typeEx.getName();
        } else if (ex instanceof MissingServletRequestParameterException missingEx) {
            userMessage = "Parametro mancante: " + missingEx.getParameterName();
        }

        return buildResponse(HttpStatus.BAD_REQUEST, userMessage);
    }
}