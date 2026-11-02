package at.fhv.eventmangement.domain.exceptions;

import java.util.Map;

//Thrown when service layer input data is invalid
//Collects all invalid fields at once so that frontend can every wrong input field in one go
public class ValidationException extends EventManagementException {
    private final Map<String, String> fieldErrors;

    // Key = field name as in EventForm, value = massage shown to user
    public ValidationException(Map<String, String> fieldErrors) {
        super("please fill mandatory fields");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
