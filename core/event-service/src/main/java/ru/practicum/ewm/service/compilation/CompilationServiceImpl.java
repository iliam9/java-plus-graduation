package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.GetCompilationsParams;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto request) {
        List<Event> events = new ArrayList<>();
        if (request.getEvents() != null) {
            events = eventRepository.findAllById(request.getEvents());
        }
        Compilation compilation = compilationMapper.mapToCompilation(request, events);
        compilation = compilationRepository.save(compilation);
        log.info("Compilation with ID = {} created", compilation.getId());
        return compilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = findCompilation(compId);
        compilationRepository.delete(compilation);
        log.info("Compilation with ID = {} deleted", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation oldEvent = findCompilation(compId);
        Set<Event> events = checkIfEventsPresent(request.getEvents());
        Compilation updated = compilationMapper.update(oldEvent,request, events);
        updated = compilationRepository.save(updated);
        log.info("Compilation with ID = {} updated", compId);
        return compilationMapper.mapToCompilationDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = findCompilation(compId);
        log.info("Compilation with ID = {} is found: {}", compId, compilation);
        return compilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(GetCompilationsParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        if (params.getPinned() == null) {
            return compilationRepository.findAll(pageable)
                    .stream()
                    .map(compilationMapper::mapToCompilationDto)
                    .toList();
        } else {
            return compilationRepository.getByPinnedOrderByPinnedAsc(params.getPinned(), pageable)
                    .stream()
                    .map(compilationMapper::mapToCompilationDto)
                    .toList();
        }
    }

    private Compilation findCompilation(long compId) {
        return compilationRepository.findById(compId).orElseThrow(() -> {
            log.error("Not found compilation with ID = {}", compId);
            return new NotFoundException("Not found compilation with ID = " + compId);
        });
    }

    private Set<Event> checkIfEventsPresent(Set<Long> eventIds) {
        if (eventIds != null && !eventIds.isEmpty()) {
            return getEvents(eventIds);
        } else {
            return Collections.emptySet();
        }
    }

    private Set<Event> getEvents(Set<Long> ids) {
        return ids.stream()
                .map(id -> eventRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Not found event with ID = " + id)))
                .collect(Collectors.toSet());
    }
}
