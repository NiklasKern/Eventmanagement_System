package at.fhv.eventmangement.exceptions;

import java.util.Map;

public class ValidationException extends EventManagementException {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("please fill mandatory fields");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
