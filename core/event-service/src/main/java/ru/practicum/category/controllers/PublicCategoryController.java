package ru.practicum.category.controllers;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.service.PublicCategoryService;
import ru.practicum.dto.category.CategoryDto;


import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class PublicCategoryController {
    public final PublicCategoryService publicCategoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") @PositiveOrZero(message = "Параметр 'from' не может быть отрицательным") int from,
                                              @RequestParam(defaultValue = "10") @Positive(message = "Параметр 'size' должен быть больше 0") int size) {
        log.info("GET-запрос к эндпоинту: '/categories' на получение categories (from = {}, size = {}", from, size);
        List<CategoryDto> response = publicCategoryService.getAllCategories(from, size);
        log.info("Сформирован ответ Get /categories с телом: {}", response);
        return response;
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoriesById(@PathVariable long catId) {
        log.info("GET-запрос к эндпоинту: '/categories/{catId}' на получение categories");
        CategoryDto response = publicCategoryService.getCategoryById(catId);
        log.info("Сформирован ответ Get /categories/{} с телом: {}", catId, response);
        return response;
    }
}
