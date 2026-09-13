package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.ProductRequest;
import com.ecommerce.ordermanagement.dto.ProductResponse;
import com.ecommerce.ordermanagement.entity.Category;
import com.ecommerce.ordermanagement.entity.Product;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.CategoryRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {

        String sku = request.getSku().trim();

        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException(
                    "Product with SKU already exists"
            );
        }

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Category not found with id: "
                                + request.getCategoryId()
                )
        );

        Product product = new Product();

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(sku);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public Page<ProductResponse> getAllProducts(
            String search,
            Long categoryId,
            Pageable pageable
    ) {

        Page<Product> products;

        boolean hasSearch =
                search != null && !search.trim().isEmpty();

        boolean hasCategory =
                categoryId != null;

        if (hasSearch && hasCategory) {

            products = productRepository
                    .findByNameContainingIgnoreCaseAndCategoryId(
                            search.trim(),
                            categoryId,
                            pageable
                    );

        } else if (hasSearch) {

            products = productRepository
                    .findByNameContainingIgnoreCase(
                            search.trim(),
                            pageable
                    );

        } else if (hasCategory) {

            products = productRepository
                    .findByCategoryId(
                            categoryId,
                            pageable
                    );

        } else {

            products = productRepository.findAll(pageable);
        }

        return products.map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        return mapToResponse(product);
    }

    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        String sku = request.getSku().trim();

        productRepository.findBySku(sku)
                .ifPresent(existingProduct -> {

                    if (!existingProduct.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Product with SKU already exists"
                        );
                    }
                });

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Category not found with id: "
                                + request.getCategoryId()
                )
        );

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(sku);
        product.setCategory(category);

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}