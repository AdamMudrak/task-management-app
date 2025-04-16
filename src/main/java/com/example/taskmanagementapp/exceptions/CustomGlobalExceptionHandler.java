package com.example.taskmanagementapp.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.example.taskmanagementapp.exceptions.badrequest.RegistrationException;
import com.example.taskmanagementapp.exceptions.conflictexpections.PasswordMismatch;
import com.example.taskmanagementapp.exceptions.forbidden.LoginException;
import com.example.taskmanagementapp.exceptions.gone.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.ActionNotFoundException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERRORS = "errors";
    private static final String SPLITERATOR = " ";
    private static final String DOT = ".";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, BAD_REQUEST);
        List<String> errors = ex
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::getErrorMessage)
                .toList();
        body.put(ERRORS, errors);
        return new ResponseEntity<>(body, headers, statusCode);
    }

    @ExceptionHandler(RegistrationException.class)
    protected ResponseEntity<Object> handleRegistrationException(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleIllegalArgument(
            RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(
                ex,
                ex.getMessage(),
                new HttpHeaders(),
                UNPROCESSABLE_ENTITY,
                request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            RuntimeException ex) {
        return getUnifiedResponse(ex, NOT_FOUND);
    }

    @ExceptionHandler(ActionNotFoundException.class)
    protected ResponseEntity<Object> handleActionNotFound(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, BAD_REQUEST);
    }

    @ExceptionHandler(NumberFormatException.class)
    protected ResponseEntity<Object> handleNumberFormatException(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatch.class)
    protected ResponseEntity<Object> handlePasswordMismatch(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, CONFLICT);
    }

    @ExceptionHandler(LinkExpiredException.class)
    protected ResponseEntity<Object> handleLinkExpired(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, GONE);
    }

    @ExceptionHandler(LoginException.class)
    protected ResponseEntity<Object> handleLoginException(
            Exception ex, WebRequest request) {
        return getUnifiedResponse(ex, FORBIDDEN);
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError fieldError) {
            String field = fieldError.getField();
            String message = e.getDefaultMessage();
            return field + SPLITERATOR + message + DOT;
        }
        return e.getDefaultMessage();
    }

    private ResponseEntity<Object> getUnifiedResponse(Exception exception, HttpStatus httpStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, httpStatus);
        String exceptionMessage = exception.getMessage();
        if (!exceptionMessage.endsWith(DOT)) {
            exceptionMessage += DOT;
        }
        body.put(ERRORS, exceptionMessage);
        return new ResponseEntity<>(body, httpStatus);
    }
}
