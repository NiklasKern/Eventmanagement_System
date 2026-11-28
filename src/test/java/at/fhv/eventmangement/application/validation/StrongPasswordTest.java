package at.fhv.eventmangement.application.validation;

import at.fhv.eventmangement.application.form.PasswordChangeForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static boolean passwordAccepted(String password) {
        PasswordChangeForm form = new PasswordChangeForm(password, password);
        return validator.validate(form).stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secret#2026", "Aa1!aaaa", "Mein Passwort 1!"})
    void acceptsPasswordsWithAllFourCharacterTypes(String password) {
        assertThat(passwordAccepted(password)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "secret#2026",   // no uppercase
            "SECRET#2026",   // no lowercase
            "Secret#word",   // no number
            "Secret2026",    // no special character
            "Aa1!",          // too short
            ""               // empty
    })
    void rejectsWeakPasswords(String password) {
        assertThat(passwordAccepted(password)).isFalse();
    }
}