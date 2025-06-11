package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {

    private final CompilationService compilationService;


    /**
     * Получить список подборок мероприятий.
     *
     * @param pinned состояние запрашиваемых подборок (закреплены на главной странице или нет)
     * @param from   смещение от начала возвращаемого списка побдорок мероприятий
     * @param size   размер возвращаемого списка побдорок мероприятий
     * @return список подборок мероприятий
     */
    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                                @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                                @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    /**
     * Получить конкретную подборку мероприятий по её идентификатору.
     *
     * @param compId идентификатор подборки мероприятий
     * @return представление запрошенной подборки мероприятий
     */
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable("compId") Long compId) {
        return compilationService.getCompilationById(compId);
    }

}
