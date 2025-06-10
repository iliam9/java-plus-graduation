package ru.practicum.ewm.controller.category;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto request) {
        log.info("Received request to create category: {}", request.getName());
        return categoryService.createCategory(request);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@Positive @PathVariable long catId) {
        log.info("Received request to delete category with ID = {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("{catId}")
    public CategoryDto updateCategory(@Valid @RequestBody NewCategoryDto request,
                                      @Positive @PathVariable long catId) {
        log.info("Received request to update category with ID = {}", catId);
        return categoryService.updateCategory(request, catId);
    }
}