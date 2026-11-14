package at.fhv.eventmangement.presentation.advice;

import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
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
@RestControllerAdvice(basePackages = "at.fhv.eventmangement.presentation.rest")
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

    // 404 - requested event does not exist
    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail handleEventNotFound(EventNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
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

    // 400 - event update rejected by business rules, reported per form field
    @ExceptionHandler(EventAlreadyExistsException.class)
    public ProblemDetail handleEventAlreadyExists(EventAlreadyExistsException ex) {
        return fieldError("name", ex.getMessage());
    }

    @ExceptionHandler(EventCategoryNotFoundException.class)
    public ProblemDetail handleEventCategoryNotFound(EventCategoryNotFoundException ex) {
        return fieldError("eventCategoryId", ex.getMessage());
    }

    @ExceptionHandler(MaxParticipantsBelowBookedPlacesException.class)
    public ProblemDetail handleMaxParticipantsBelowBooked(MaxParticipantsBelowBookedPlacesException ex) {
        return fieldError("maxParticipants", ex.getMessage());
    }

    @ExceptionHandler(EventDateHasBookingsException.class)
    public ProblemDetail handleEventDateHasBookings(EventDateHasBookingsException ex) {
        return fieldError("eventDates", ex.getMessage());
    }

    private static ProblemDetail fieldError(String field, String message) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", Map.of(field, message));
        return pd;
    }
}
