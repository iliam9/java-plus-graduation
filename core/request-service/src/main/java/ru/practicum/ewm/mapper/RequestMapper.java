package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "request.userId", target = "requester")
    @Mapping(source = "request.eventId", target = "event")
    ParticipationRequestDto mapToDto(Request request);

    @Mapping(source = "confirmedRequests", target = "confirmedRequests")
    @Mapping(source = "rejectedRequests", target = "rejectedRequests")
    EventRequestStatusUpdateResult mapToRequestStatus(List<Request> confirmedRequests, List<Request> rejectedRequests);

    List<ParticipationRequestDto> mapToDto(List<Request> requests);
}
