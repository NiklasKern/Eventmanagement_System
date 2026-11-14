package at.fhv.eventmangement.application.dto;

import java.util.List;

public record EventUpdateResultDto(EventDto event, List<String> notifiedCustomers) {

}
