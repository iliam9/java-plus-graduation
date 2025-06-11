package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;


    /**
     * Добавить новую категорию (раздел) мероприятий в систему.
     *
     * @param dto представление создаваемой категории (раздела) мероприятий
     * @return представление только что добавленной категории (раздела) мероприятий
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto saveCategory(@Valid @RequestBody NewCategoryDto dto) {
        return categoryService.saveCategory(dto);
    }

    /**
     * Удалить существующую категорию (раздел) мероприятий.
     *
     * @param catId идентификатор удаляемой категории (раздела) мероприятий
     */
    @DeleteMapping("/{catID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("catID") Long catId) {
        categoryService.deleteCategory(catId);
    }

    /**
     * Обновить информацию о существующей в системе категории (разделе) мероприятий.
     *
     * @param catId идентификатор обновляемой категории (раздела) мероприятий
     * @param dto   представление обновляемой категории (раздела) мероприятий
     * @return представление только что обновленной категории (раздела) мероприятий
     */
    @PatchMapping("/{catID}")
    public CategoryDto updateCategory(@PathVariable("catID") Long catId,
                                      @Valid @RequestBody CategoryDto dto) {
        return categoryService.updateCategory(catId, dto);
    }

}
