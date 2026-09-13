package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Category;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByNameIgnoreCase_shouldReturnCategory_whenNameExists() {
        String categoryName = "Repository Test Category " + System.nanoTime();

        Category category = new Category(
                categoryName,
                "Repository test description"
        );

        categoryRepository.save(category);

        Optional<Category> result =
                categoryRepository.findByNameIgnoreCase(
                        categoryName.toLowerCase()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo(categoryName);
        assertThat(result.get().getDescription())
                .isEqualTo("Repository test description");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnEmpty_whenNameDoesNotExist() {
        Optional<Category> result =
                categoryRepository.findByNameIgnoreCase(
                        "Does Not Exist " + System.nanoTime()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnTrue_whenNameExists() {
        String categoryName = "Existing Category " + System.nanoTime();

        Category category = new Category(
                categoryName,
                "Existing category description"
        );

        categoryRepository.save(category);

        assertThat(
                categoryRepository.existsByNameIgnoreCase(
                        categoryName.toLowerCase()
                )
        ).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnFalse_whenNameDoesNotExist() {
        assertThat(
                categoryRepository.existsByNameIgnoreCase(
                        "Missing Category " + System.nanoTime()
                )
        ).isFalse();
    }
}