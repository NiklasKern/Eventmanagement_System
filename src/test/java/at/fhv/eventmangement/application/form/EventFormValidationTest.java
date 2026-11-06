package at.fhv.eventmangement.application.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the Validation rules on EventForm;
 * mandatory fields, "please fill mandatory fields", price > 0, capacity > 0, date in the future.
 */
class EventFormValidationTest {

    private static final String MANDATORY = "please fill mandatory fields";

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static EventForm validForm() {
        return EventForm.builder()
                .name("Summer Hike")
                .description("A nice walk")
                .location("Dornbirn")
                .eventDates(List.of(EventDateForm.builder()
                        .startTime(LocalDateTime.now().plusDays(10))
                        .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                        .build()
                ))
                .price(new BigDecimal("25.00"))
                .minParticipants(3)
                .maxParticipants(20)
                .eventCategoryId(UUID.randomUUID())
                .build();
    }

    /** field name -> message, first message per field */
    private static Map<String, String> errorsOf(EventForm form) {
        Set<ConstraintViolation<EventForm>> violations = validator.validate(form);
        return violations.stream().collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (first, second) -> first));
    }

    @Test
    void validForm_hasNoErrors() {
        assertThat(validator.validate(validForm())).isEmpty();
    }

    @Test
    void emptyForm_reportsEveryMandatoryFieldWithMandatoryMessage() {
        Map<String, String> errors = errorsOf(new EventForm());

        assertThat(errors)
                .containsEntry("name", MANDATORY)
                .containsEntry("location", MANDATORY)
                .containsEntry("eventDates", "please add at least one date")
                .containsEntry("price", MANDATORY)
                .containsEntry("minParticipants", MANDATORY)
                .containsEntry("maxParticipants", MANDATORY)
                .containsEntry("eventCategoryId", MANDATORY);
    }

    @Test
    void description_isOptional() {
        EventForm form = validForm();
        form.setDescription(null);

        assertThat(errorsOf(form)).doesNotContainKey("description");
    }

    @Test
    void blankName_isRejected() {
        EventForm form = validForm();
        form.setName("   ");

        assertThat(errorsOf(form)).containsEntry("name", MANDATORY);
    }

    @Test
    void blankLocation_isRejected() {
        EventForm form = validForm();
        form.setLocation("");

        assertThat(errorsOf(form)).containsEntry("location", MANDATORY);
    }

    @Test
    void zeroPrice_isRejected() {
        EventForm form = validForm();
        form.setPrice(BigDecimal.ZERO);

        assertThat(errorsOf(form)).containsEntry("price", "must be greater than 0");
    }

    @Test
    void negativePrice_isRejected() {
        EventForm form = validForm();
        form.setPrice(new BigDecimal("-5"));

        assertThat(errorsOf(form)).containsKey("price");
    }

    @Test
    void smallPositivePrice_isAccepted() {
        EventForm form = validForm();
        form.setPrice(new BigDecimal("0.01"));

        assertThat(errorsOf(form)).doesNotContainKey("price");
    }

    @Test
    void zeroCapacity_isRejected() {
        EventForm form = validForm();
        form.setMaxParticipants(0);

        assertThat(errorsOf(form)).containsEntry("maxParticipants", "must be greater than 0");
    }

    @Test
    void zeroMinimumParticipants_isRejected() {
        EventForm form = validForm();
        form.setMinParticipants(0);

        assertThat(errorsOf(form)).containsEntry("minParticipants", "must be greater than 0");
    }

    @Test
    void pastDate_isRejected() {
        EventForm form = validForm();
        form.setEventDates(List.of(EventDateForm.builder()
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(3))
                .build()
        ));
        assertThat(errorsOf(form)).containsEntry("eventDates[0].startTime", "Date has to be in the future");
    }

    @Test
    void minGreaterThanMax_isRejected() {
        EventForm form = validForm();
        form.setMinParticipants(30);
        form.setMaxParticipants(20);

        assertThat(errorsOf(form)).containsEntry("maxParticipantsValid", "must not be less than minimum participants");
    }
}
