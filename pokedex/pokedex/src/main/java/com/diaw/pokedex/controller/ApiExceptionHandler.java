package com.diaw.pokedex.controller;

import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> domain(ResponseStatusException error) {
        return problem(error.getStatusCode(), error.getReason());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> duplicate() {
        return problem(HttpStatus.CONFLICT, "Já existe um Pokémon com esse ID.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException error) {
        String detail = error.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Dados inválidos. " + detail);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> malformed() {
        return problem(HttpStatus.BAD_REQUEST, "Verifique o JSON enviado e o ID numérico da URL.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> database() {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "MongoDB indisponível. Tente novamente em instantes.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatusCode status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
