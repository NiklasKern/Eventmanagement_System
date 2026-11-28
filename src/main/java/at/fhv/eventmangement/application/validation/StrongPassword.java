package at.fhv.eventmangement.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@NotBlank
@Size(min = 8, max = 128)
@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).*$")
@ReportAsSingleViolation //so it only composes a single constraint message on fail
public @interface StrongPassword { //@interface is an annotation rather than a real interface, needed so we can use @StrongPassword
    String message() default "password must have 8-128 characters, contain an uppercase and lowercase letter, a number and a special character!";

    Class<?>[] groups() default {}; //defines validation group
    Class<? extends Payload>[] payload() default{}; //for extensibility
}
