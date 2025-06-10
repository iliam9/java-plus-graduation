package ru.practicum.ewm.service.category;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto request);

    void deleteCategory(long id);

    CategoryDto updateCategory(NewCategoryDto request, long id);

    CategoryDto findCategoryById(long id);

    List<CategoryDto> findCategories(int from, int size);
}
