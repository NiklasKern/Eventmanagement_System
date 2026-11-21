package at.fhv.eventmangement.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Argon2PasswordHasherTest {

    private final Argon2PasswordHasher hasher = new Argon2PasswordHasher(new PasswordConfig().passwordEncoder());

    @Test
    void hashIsArgon2id_andNotThePlainPassword() {
        String hash = hasher.hash("Secret#2026");

        assertThat(hash).startsWith("$argon2id$").doesNotContain("Secret#2026");
    }

    @Test
    void matchesOnlyTheRightPassword() {
        String hash = hasher.hash("Secret#2026");

        assertThat(hasher.matches("Secret#2026", hash)).isTrue();
        assertThat(hasher.matches("secret#2026", hash)).isFalse();
    }

    @Test
    void samePasswordGivesDifferentHashes_becauseOfTheRandomSalt() {
        assertThat(hasher.hash("Secret#2026")).isNotEqualTo(hasher.hash("Secret#2026"));
    }
}