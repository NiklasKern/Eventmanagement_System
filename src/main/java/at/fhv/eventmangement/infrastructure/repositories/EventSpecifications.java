package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.domain.repositories.EventSearchCriteria;
import at.fhv.eventmangement.infrastructure.entities.EventCategoryEntity;
import at.fhv.eventmangement.infrastructure.entities.EventDateEntity;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Turns EventSearchCriteria into database query
public class EventSpecifications {
    private static final char ESCAPE = '\\';

    private EventSpecifications() {
    }

    static Specification<EventEntity> matching(EventSearchCriteria searchCriteria) {
        List<Specification<EventEntity>> specifications = new ArrayList<>();
        if (searchCriteria.status() != null) {
            specifications.add(hasStatus(searchCriteria.status()));
        }
        if (searchCriteria.text() != null) {
            specifications.add(containstext(searchCriteria.text()));
        }
        if (searchCriteria.location() != null) {
            specifications.add(locationContains(searchCriteria.location()));
        }
        if (searchCriteria.categoryId() != null) {
            specifications.add(hasCategory(searchCriteria.categoryId()));
        }
        if (searchCriteria.priceFrom() != null) {
            specifications.add(priceAtLeast(searchCriteria.priceFrom()));
        }
        if (searchCriteria.priceTo() != null) {
            specifications.add(priceAtMost(searchCriteria.priceTo()));
        }
        if (searchCriteria.from() != null || searchCriteria.to() != null) {
            specifications.add(hasDateBetween(searchCriteria.from(), searchCriteria.to()));
        }
        return Specification.allOf(specifications);
    }

    private static Specification<EventEntity> priceAtMost(BigDecimal price) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), price);
    }

    private static Specification<EventEntity> priceAtLeast(BigDecimal price) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), price);
    }

    private static Specification<EventEntity> hasCategory(UUID categoryId) {
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("eventCategory").get("id"), categoryId);
    }

    private static Specification<EventEntity> locationContains(String location) {
        return ((root, query, criteriaBuilder)
                -> criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), likePattern(location), ESCAPE));
    }

    private static Specification<EventEntity> containstext(String text) {
        return ((root, query, criteriaBuilder) -> {
            String pattern = likePattern(text);
            Join<EventEntity, EventCategoryEntity> category = root.join("eventCategory");
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern, ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern, ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), pattern, ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(category.get("categoryName")), pattern, ESCAPE));
        });
    }

    private static Specification<EventEntity> hasStatus(EventStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("eventStatus"), status);
    }

    private static Specification<EventEntity> hasDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<EventDateEntity> date = sub.from(EventDateEntity.class);

            List<Predicate> conditions = new ArrayList<>();
            conditions.add(criteriaBuilder.equal(date.get("event"), root));
            if (from != null) {
                conditions.add(criteriaBuilder.greaterThanOrEqualTo(date.get("endTime"), from));
            }
            if (to != null) {
                conditions.add(criteriaBuilder.lessThan(date.get("startTime"), to));
            }

            sub.select(date.get("id")).where(conditions.toArray(Predicate[]::new));
            return criteriaBuilder.exists(sub);
        };
    }

    private static String likePattern(String value) {
        String escaped = value.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
