package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.dto.CategoryDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto map(Category category);

    @Mapping(target = "id", ignore = true)
    Category mapToCategory(NewCategoryDto request);

    List<CategoryDto> mapToCategoryDto(List<Category> categories);
}
