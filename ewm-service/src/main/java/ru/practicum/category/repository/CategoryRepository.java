package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByName(String name);

    Integer countByName(String name);

    Boolean existsByName(String name);
}
