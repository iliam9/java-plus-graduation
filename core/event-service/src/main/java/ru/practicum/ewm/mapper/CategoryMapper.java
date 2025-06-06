package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.dto.category.CategoryDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto map(Category category);

    @Mapping(target = "id", ignore = true)
    Category mapToCategory(NewCategoryDto request);

    List<CategoryDto> mapToCategoryDto(List<Category> categories);
}
