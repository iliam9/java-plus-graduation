package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

import java.util.List;

@UtilityClass
public class ParticipationRequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequesterId())
                .event(request.getEventId())
                .created(request.getCreated())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationRequestDto(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }
}
