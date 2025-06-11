package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.interaction.api.dto.compilation.CompilationDto;
import ru.practicum.interaction.api.dto.compilation.NewCompilationDto;
import ru.practicum.interaction.api.dto.compilation.UpdateCompilationRequest;
import ru.practicum.interaction.api.dto.event.EventShortDto;
import ru.practicum.interaction.api.dto.user.UserDto;
import ru.practicum.interaction.api.exception.NotFoundException;
import ru.practicum.interaction.api.feignClient.client.user.UserClient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final UserClient userClient;

    private final String COMPILATION_NOT_FOUND = "Подборка не найдена.";

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Set<Long> eventIds = newCompilationDto.getEvents();
        List<Event> events;
        if (eventIds != null && !eventIds.isEmpty()) {
            events = getSeveralEvents(eventIds.stream().toList());
        } else {
            events = Collections.emptyList();
        }
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto, events));
        return CompilationMapper.toCompilationDto(compilation, mapToEventShort(events));

    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(COMPILATION_NOT_FOUND));
        Set<Long> eventIds = updateCompilationRequest.getEvents();
        if (eventIds != null && !eventIds.isEmpty()) {
            compilation.setEvents(new HashSet<>(getSeveralEvents(eventIds.stream().toList())));
        }
        Boolean pinned = updateCompilationRequest.getPinned();
        if (pinned != null) {
            compilation.setPinned(pinned);
        }
        String title = updateCompilationRequest.getTitle();
        if (title != null && !title.isBlank()) {
            compilation.setTitle(title);
        }
        return CompilationMapper.toCompilationDto(compilation, mapToEventShort(new ArrayList<>(compilation.getEvents())));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(COMPILATION_NOT_FOUND);
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Compilation> allCompilations;
        if (pinned == null) {
            allCompilations = compilationRepository.findAll(pageRequest).toList();
        } else {
            allCompilations = compilationRepository.findAllByPinned(pageRequest, pinned);
        }
        Map<Long, EventShortDto> allEventDto = new HashMap<>();
        mapToEventShort(allCompilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream()).toList()).stream()
                .toList().forEach(event -> {
                    if (!allEventDto.containsKey(event.getId())) {
                        allEventDto.put(event.getId(), event);
                    }
                });
        List<CompilationDto> compilationDtoList = new ArrayList<>();
        for (Compilation compilation : allCompilations) {
            List<EventShortDto> listEventDto = compilation.getEvents().stream().map(event -> allEventDto.get(event.getId()))
                    .toList();
            compilationDtoList.add(CompilationMapper.toCompilationDto(compilation, listEventDto));
        }
        return compilationDtoList;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(COMPILATION_NOT_FOUND));
        return CompilationMapper.toCompilationDto(compilation, mapToEventShort(new ArrayList<>(compilation.getEvents())));
    }

    private List<Event> getSeveralEvents(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        if (events.size() != eventIds.size()) {
            throw new NotFoundException("Не удалось найти некоторые события в базе данных");
        }
        return events;
    }

    private List<EventShortDto> mapToEventShort(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();
        Map<Long, UserDto> mapUserDto = userClient.getAllUsers(userIds, 0, userIds.size())
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return events.stream().map(event ->
                EventMapper.mapToShortDto(event, 0d, mapUserDto.getOrDefault(event.getInitiatorId(), UserDto.builder().id(event.getInitiatorId()).name("UNKNOWN").build()))).toList();
    }
}

