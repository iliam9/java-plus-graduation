package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.User;

import java.util.List;

public interface UserRepository  extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findAllByIds(@Param("ids") List<Long> ids);

    @Query("SELECT u FROM User u ORDER BY u.id LIMIT :size OFFSET :from")
    List<User> findAllOrderById(@Param("from") int from, @Param("size") int size);

    boolean existsByEmail(String email);
}
