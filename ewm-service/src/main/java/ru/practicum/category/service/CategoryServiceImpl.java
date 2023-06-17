package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequestDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {

    final CategoryRepository categoryRepository;
    final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryRequestDto newCategoryRequestDto) {
        if (categoryRepository.existsByName(newCategoryRequestDto.getName())) {
            throw new ConflictException("Категория " + newCategoryRequestDto.getName() + " уже существует");
        }
        Category category = categoryRepository.save(CategoryDtoMapper.mapNewDtoToCategory(newCategoryRequestDto));
        log.info("Категория сохранена " + category.getId());
        return CategoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public void deleteCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория не существует " + catId)
        );
        if (!eventRepository.existsByCategoryId(catId)) {
            categoryRepository.deleteById(catId);
            log.info("Категория удалена " + catId);
        } else {
            throw new ConflictException("Категорию с привязанными событиями не удалить " + catId);
        }
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория " + catId + " не найдена")
        );
        List<Category> byName = categoryRepository.findByName(categoryDto.getName());
        Long id = null;
        if (byName.size() > 0) {
            id = byName.stream()
                    .filter((e) -> Objects.equals(e.getId(), catId))
                    .findFirst()
                    .orElseThrow(
                            () -> new ConflictException("Название категории " + categoryDto.getName() + " уже существует")
                    ).getId();
        }
        category.setName(categoryDto.getName());
        return CategoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(CategoryDtoMapper::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория " + catId + " не найдена"));
        return CategoryDtoMapper.mapCategoryToDto(category);
    }
}
