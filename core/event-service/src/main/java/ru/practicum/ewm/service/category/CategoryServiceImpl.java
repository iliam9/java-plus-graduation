package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto request) {
        Category category = categoryMapper.mapToCategory(request);
        checkForCategoryDuplicates(category.getName());
        category = categoryRepository.save(category);
        log.info("Category is saved: {}", category.getName());
        return categoryMapper.map(category);
    }

    @Override
    public void deleteCategory(long id) {
        Category categoryToDelete = findCategory(id);
        checkForLinkedEvents(id);
        categoryRepository.delete(categoryToDelete);
        log.info("Category with ID = {} is deleted", id);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto request, long id) {
        Category category = findCategory(id);
        if (!category.getName().equals(request.getName())) {
            checkForCategoryDuplicates(request.getName());
        }
        category.setName(request.getName());
        categoryRepository.save(category);
        log.info("Category with ID = {} is updated", id);
        return categoryMapper.map(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findCategoryById(long id) {
        Category category = findCategory(id);
        log.info("Category with ID = {} is found: {}", id, category.getName());
        return categoryMapper.map(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findCategories(int from, int size) {
        List<Category> categories = categoryRepository.findAllOrderById(from, size);
        log.info("Search for categories with params: from = {}, size = {} completed", from, size);
        return categoryMapper.mapToCategoryDto(categories);
    }

    private Category findCategory(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category with ID = {} is not found ", id);
                    return new NotFoundException("Not found category with ID = " + id);
                });
    }

    private void checkForCategoryDuplicates(String categoryName) {
        boolean isDuplicate = categoryRepository.existsByNameIgnoreCase(categoryName);
        if (isDuplicate) {
            log.error("Category with name: {} already exists", categoryName);
            throw new ConflictDataException("This category already exists");
        }
    }

    private void checkForLinkedEvents(long id) {
        boolean areEventsLinked = eventRepository.existsByCategoryId(id);
        if (areEventsLinked) {
            log.error("There are events in this category");
            throw new ConflictDataException("You cannot delete this category, there are linked events");
        }
    }

}
