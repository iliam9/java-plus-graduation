package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c ORDER BY c.id LIMIT :size OFFSET :from")
    List<Category> findAllOrderById(@Param("from") int from, @Param("size") int size);
}
