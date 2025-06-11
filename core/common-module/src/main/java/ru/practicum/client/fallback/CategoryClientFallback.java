package ru.practicum.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.CategoryClient;
import ru.practicum.dto.categories.CategoryDto;

@Component
@Slf4j
public class CategoryClientFallback implements CategoryClient {

    private static final String SERVICE_UNAVAILABLE = "Сервис 'Категории' временно недоступен: ";


    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.warn(SERVICE_UNAVAILABLE + "невозможно получить категорию с id = {}.",
                catId);
        return null;
    }

}
