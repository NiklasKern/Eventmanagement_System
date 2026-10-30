package at.fhv.eventmangement.infrastructure;

import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class BookingNumberGenerator {

    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();
    private final EventBookingRepository eventBookingRepository;

    public BookingNumberGenerator(EventBookingRepository eventBookingRepository) {
        this.eventBookingRepository = eventBookingRepository;
    }

    public String next() {
        String number;
        do {
            number = "BK-" + randomCode();
        } while (eventBookingRepository.existsByBookingNumber(number));
        return number;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}