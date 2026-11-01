package at.fhv.eventmangement.application.exceptions;

import java.util.Map;

// Thrown when user input cant be accepted (duplicate, min > max, etc.)
public class InvalidInputException  extends RuntimeException{
    private final Map<String, String> fieldErrors;

    public InvalidInputException(Map<String, String> fiedErrors) {
        super("Invalid input");
        this.fieldErrors = fiedErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
