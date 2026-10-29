package at.fhv.eventmangement.controllers.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.util.Map;
import java.util.stream.Collectors;

// Central error handler for all REST controllers
// Thymeleaf controllers are excluded – they show errors in the HTML form instead.
// Unexpected errors in /event/manage/create now show Springs default error page (for our own design we could add templates/error.html)
@RestControllerAdvice(basePackages = "at.fhv.eventmangement.controllers.rest")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @Validated service rejected its input
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (first, second) -> first));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    // 400 - Valid on a RequestBody failed (@NotBlank, @Positive, ...)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // Converts Springs field errors into a map: field -> message
        // Keeps only the first error when several errors are thrown
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        e -> e.getField(),
                        e -> e.getDefaultMessage() == null ? "invalid" : e.getDefaultMessage(),
                        (first, second) -> first));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    // 500 - anything else: see log details for details
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
