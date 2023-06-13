package ru.practicum.category.service;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequestDto;
import ru.practicum.category.model.Category;


@Component
public class CategoryDtoMapper {
    public static Category mapNewDtoToCategory(NewCategoryRequestDto categoryDto) {
        return Category.builder()
                .id(null)
                .name(categoryDto.getName())
                .build();
    }

    public static CategoryDto mapCategoryToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
