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
import ru.practicum.event.model.Event;
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
    final CategoryDtoMapper categoryDtoMapper;
    final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryRequestDto newCategoryRequestDto) {
        if (categoryRepository.findByName(newCategoryRequestDto.getName()).size() > 0) {
            throw new ConflictException("Категория " + newCategoryRequestDto.getName() + " уже существует");
        }
        Category category = categoryRepository.save(categoryDtoMapper.mapNewDtoToCategory(newCategoryRequestDto));
        log.info("Категория сохранена " + category.getId());
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public void deleteCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория не существует " + catId)
        );
        List<Event> eventsByCat = eventRepository.findAllByCategoryId(catId);
        if (eventsByCat.isEmpty()) {
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
        //в условии исправить ошибку. Если обновляется то же самое название на само себя, то ОК
        //Если чужое название обновляется на существующее, то conflict
        List<Category> byName = categoryRepository.findByName(categoryDto.getName());
        Long id = null;
        if (byName.size() > 0) {
            id = byName.stream()
                    .filter((e) -> Objects.equals(e.getId(), catId))
                    .findFirst()
                    .orElseThrow(
                            () -> new ConflictException("Название категории " + categoryDto.getName() + " уже существует")
                    ).getId();
            if (catId != id) {
                throw new ConflictException("Название категории " + categoryDto.getName() + " уже существует");
            }
        }
        category.setName(categoryDto.getName());
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(categoryDtoMapper::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория " + catId + " не найдена"));
        return categoryDtoMapper.mapCategoryToDto(category);
    }
}
