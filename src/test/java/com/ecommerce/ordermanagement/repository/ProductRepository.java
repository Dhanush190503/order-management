package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Category;
import com.ecommerce.ordermanagement.entity.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Repository Test Category " + System.nanoTime());
        category = categoryRepository.save(category);
    }

    @Test
    void existsBySku_shouldReturnTrue_whenSkuExists() {
        Product product = new Product(
                "Test Product",
                "Test product description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-" + System.nanoTime(),
                category
        );

        productRepository.save(product);

        assertThat(productRepository.existsBySku(product.getSku()))
                .isTrue();
    }

    @Test
    void existsBySku_shouldReturnFalse_whenSkuDoesNotExist() {
        assertThat(productRepository.existsBySku(
                "MISSING-SKU-" + System.nanoTime()
        )).isFalse();
    }

    @Test
    void findBySku_shouldReturnProduct_whenSkuExists() {
        String sku = "FIND-SKU-" + System.nanoTime();

        Product product = new Product(
                "Find Product",
                "Product for findBySku test",
                new BigDecimal("49.99"),
                20,
                sku,
                category
        );

        productRepository.save(product);

        var result = productRepository.findBySku(sku);

        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo(sku);
        assertThat(result.get().getName()).isEqualTo("Find Product");
    }

    @Test
    void findBySku_shouldReturnEmpty_whenSkuDoesNotExist() {
        var result = productRepository.findBySku(
                "MISSING-FIND-SKU-" + System.nanoTime()
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_shouldFindMatchingProducts() {
        String uniqueWord = "UniqueRepository" + System.nanoTime();

        Product product1 = new Product(
                uniqueWord + " Product One",
                "First test product",
                new BigDecimal("10.00"),
                5,
                "NAME-SKU-1-" + System.nanoTime(),
                category
        );

        Product product2 = new Product(
                uniqueWord + " Product Two",
                "Second test product",
                new BigDecimal("20.00"),
                10,
                "NAME-SKU-2-" + System.nanoTime(),
                category
        );

        productRepository.save(product1);
        productRepository.save(product2);

        Page<Product> result =
                productRepository.findByNameContainingIgnoreCase(
                        uniqueWord.toLowerCase(),
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder(
                        uniqueWord + " Product One",
                        uniqueWord + " Product Two"
                );
    }

    @Test
    void findByCategoryId_shouldFindProductsInCategory() {
        Product product1 = new Product(
                "Category Product One",
                "First category test product",
                new BigDecimal("30.00"),
                5,
                "CATEGORY-SKU-1-" + System.nanoTime(),
                category
        );

        Product product2 = new Product(
                "Category Product Two",
                "Second category test product",
                new BigDecimal("40.00"),
                8,
                "CATEGORY-SKU-2-" + System.nanoTime(),
                category
        );

        productRepository.save(product1);
        productRepository.save(product2);

        Page<Product> result =
                productRepository.findByCategoryId(
                        category.getId(),
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Product::getCategory)
                .allMatch(foundCategory ->
                        foundCategory.getId().equals(category.getId())
                );
    }

    @Test
    void findByNameContainingIgnoreCaseAndCategoryId_shouldFindMatchingProducts() {
        String uniqueWord = "FilteredRepository" + System.nanoTime();

        Product matchingProduct = new Product(
                uniqueWord + " Matching Product",
                "Matching filtered product",
                new BigDecimal("59.99"),
                15,
                "FILTER-SKU-1-" + System.nanoTime(),
                category
        );

        Product nonMatchingProduct = new Product(
                "Different Product",
                "Non matching filtered product",
                new BigDecimal("69.99"),
                12,
                "FILTER-SKU-2-" + System.nanoTime(),
                category
        );

        productRepository.save(matchingProduct);
        productRepository.save(nonMatchingProduct);

        Page<Product> result =
                productRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        uniqueWord.toLowerCase(),
                        category.getId(),
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Product::getName)
                .isEqualTo(uniqueWord + " Matching Product");
    }
}