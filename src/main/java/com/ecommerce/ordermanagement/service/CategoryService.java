package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.CategoryRequest;
import com.ecommerce.ordermanagement.dto.CategoryResponse;
import com.ecommerce.ordermanagement.entity.Category;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Category already exists"
            );
        }

        Category category = new Category();

        category.setName(name);
        category.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );

        Category savedCategory =
                categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + id
                                )
                        );

        return mapToResponse(category);
    }

    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + id
                                )
                        );

        String name = request.getName().trim();

        categoryRepository.findByNameIgnoreCase(name)
                .ifPresent(existingCategory -> {

                    if (!existingCategory.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Category already exists"
                        );
                    }
                });

        category.setName(name);

        category.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );

        Category updatedCategory =
                categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + id
                                )
                        );

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(
            Category category
    ) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}