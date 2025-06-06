package ru.practicum.ewm.controller.compilation;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.GetCompilationsParams;
import ru.practicum.ewm.service.compilation.CompilationService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(defaultValue = "10", required = false) Integer size,
                                                @RequestParam(defaultValue = "0", required = false) Integer from,
                                                @RequestParam(required = false) Boolean pinned) {
        GetCompilationsParams params = new GetCompilationsParams(size, from, pinned);
        log.info("Received request to find compilations with parameters: {}", params);
        return compilationService.getCompilations(params);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@Positive @PathVariable Long compId) {
        log.info("Received request to find compilation with ID = {}", compId);
        return compilationService.getCompilation(compId);
    }

}
