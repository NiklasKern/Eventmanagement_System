package at.fhv.eventmangement.application.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchForm {
    private String text;
    private String location;
    private UUID categoryId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    @PositiveOrZero(message = "price must not be negative")
    private BigDecimal priceFrom;

    @PositiveOrZero(message = "price must not be negative")
    private BigDecimal priceTo;

    @AssertTrue(message = "price from must not be higher than price to")
    public boolean isPriceRangeValid() {
        return priceFrom == null || priceTo == null || priceFrom.compareTo(priceTo) <= 0;
    }

    @AssertTrue(message = "date from must not be greater than date to")
    public boolean isDateRangeValid() {
        return dateFrom == null || dateTo == null || !dateFrom.isAfter(dateTo);
    }

    public boolean isExtended() {
        return priceFrom != null || priceTo != null;
    }
}
