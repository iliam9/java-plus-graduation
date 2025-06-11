package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryPublicController {

    private final CategoryService categoryService;


    /**
     * Получить список всех категорий (разделов) мероприятий, существующих в системе.
     *
     * @param from смещение от начала возвращаемого списка категорий (разделов) мероприятий
     * @param size размер возвращаемого списка категорий (разделов) мероприятий
     * @return список категорий (разделов) мероприятий
     */
    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                           @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return categoryService.getCategories(from, size);
    }

    /**
     * Получить информацию о конкретной категории (разделе) мероприятий по её идентификатору.
     *
     * @param catId идентификатор категории (раздела) мероприятий
     * @return представление запрошенной категории (раздела) мероприятий
     */
    @GetMapping("/{catID}")
    public CategoryDto getCategoryById(@PathVariable("catID") Long catId) {
        return categoryService.getCategoryById(catId);
    }

}
