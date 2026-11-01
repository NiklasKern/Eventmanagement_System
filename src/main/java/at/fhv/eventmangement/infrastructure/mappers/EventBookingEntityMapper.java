package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.EventBooking;
import at.fhv.eventmangement.infrastructure.entities.EventBookingEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CustomerEntityMapper.class, EventDateEntityMapper.class, EventParticipantEntityMapper.class})
public interface EventBookingEntityMapper {
    EventBooking toDomain(EventBookingEntity entity);
    EventBookingEntity toEntity(EventBooking domain);

    //Participant needs the reference to its booking
    @AfterMapping
    default void linkParticipants(@MappingTarget EventBookingEntity booking) {
        if (booking.getEventParticipants() != null) {
            booking.getEventParticipants().forEach(p -> p.setEventBooking(booking));
        }
    }
}
