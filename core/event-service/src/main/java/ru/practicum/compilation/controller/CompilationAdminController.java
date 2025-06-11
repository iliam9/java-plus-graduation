package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationService compilationService;


    /**
     * Добавить подборку мероприятий.
     *
     * @param dto представление добавляемой подборки мероприятий
     * @return прдеставление добавленной подборки мероприятий
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto dto) {
        return compilationService.saveCompilation(dto);
    }

    /**
     * Удалить подборку мероприятий.
     *
     * @param compId идентификатор удаляемой подборки мероприятий
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable("compId") Long compId) {
        compilationService.deleteCompilation(compId);
    }

    /**
     * Обновить подборку мероприятий.
     *
     * @param compId идентификатор обновляемой подборки мероприятий
     * @param dto    представление обновляемой подборки мероприятий
     * @return представление обновленной подборки мероприятий
     */
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable("compId") Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest dto) {
        return compilationService.updateCompilation(compId, dto);
    }

}
