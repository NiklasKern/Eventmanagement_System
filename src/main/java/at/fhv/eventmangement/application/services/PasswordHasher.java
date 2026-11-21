package at.fhv.eventmangement.application.services;

// Turns plain to hash pw
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String passwordHash);
}
