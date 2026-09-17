package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.auth.AuthException;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(AuthException.class) ProblemDetail auth(AuthException ex) {
        return switch (ex.reason()) {
            case EMAIL_EXISTS -> problem(HttpStatus.CONFLICT, "Email already registered");
            case DOCUMENT_EXISTS -> problem(HttpStatus.CONFLICT, "Document already registered");
            default -> problem(HttpStatus.UNAUTHORIZED, "Authentication failed");
        };
    }
    @ExceptionHandler(InvalidCsrfException.class) ProblemDetail csrf() { return problem(HttpStatus.UNAUTHORIZED, "Authentication failed"); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage())).toList());
        return detail;
    }
    private ProblemDetail problem(HttpStatus status, String title) { ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, title); detail.setTitle(title); return detail; }
}
