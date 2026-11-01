package at.fhv.eventmangement.application.exceptions;

import java.util.Map;

// Thrown when user input cant be accepted (duplicate, min > max, etc.)
public class InvalidInputException  extends RuntimeException{
    private final Map<String, String> fiedErrors;

    public InvalidInputException(Map<String, String> fiedErrors) {
        super("Invalid input");
        this.fiedErrors = fiedErrors;
    }

    public Map<String, String> getFiedErrors() {
        return fiedErrors;
    }
}
