package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.client.fallback.CategoryClientFallback;
import ru.practicum.dto.categories.CategoryDto;

@FeignClient(name = "category-service", fallback = CategoryClientFallback.class)
public interface CategoryClient {

    /**
     * Получение конкретной категории по ее идентификатору.
     *
     * @param catId идентификатор категории
     * @return категория.
     */
    @GetMapping("/categories/{catID}")
    CategoryDto getCategoryById(@PathVariable("catID") Long catId);

}
