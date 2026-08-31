package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.CategoryRequest;
import io.everyonecodes.project_module.dtos.responses.CategoryResponse;
import io.everyonecodes.project_module.models.Category;
import io.everyonecodes.project_module.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String cleanName = request.getName().trim();
        if (categoryRepository.existsByName(cleanName)) {
            throw new IllegalArgumentException("Category '" + cleanName + "' already exists.");
        }

        Category category = Category.builder()
                .name(cleanName)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}