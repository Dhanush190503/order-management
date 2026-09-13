package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Cart;
import com.ecommerce.ordermanagement.entity.CartItem;
import com.ecommerce.ordermanagement.entity.Category;
import com.ecommerce.ordermanagement.entity.Product;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new IllegalStateException("CUSTOMER role not found")
                );

        User user = new User();
        user.setName("Cart Item Test User " + System.nanoTime());
        user.setEmail(
                "cart-item-test-" + System.nanoTime() + "@test.com"
        );
        user.setPassword("encoded-password");
        user.setRole(customerRole);
        user = userRepository.save(user);

        cart = new Cart(user);
        cart = cartRepository.save(cart);

        Category category = new Category(
                "Cart Item Test Category " + System.nanoTime(),
                "Cart item repository test category"
        );
        category = categoryRepository.save(category);

        product = new Product(
                "Cart Item Test Product",
                "Cart item repository test product",
                new BigDecimal("99.99"),
                20,
                "CART-ITEM-SKU-" + System.nanoTime(),
                category
        );
        product = productRepository.save(product);
    }

    @Test
    void findByCartIdAndProductId_shouldReturnCartItem_whenExists() {
        CartItem cartItem = new CartItem(product, 3);
        cart.addItem(cartItem);

        cartItemRepository.save(cartItem);

        var result = cartItemRepository.findByCartIdAndProductId(
                cart.getId(),
                product.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId())
                .isEqualTo(cart.getId());
        assertThat(result.get().getProduct().getId())
                .isEqualTo(product.getId());
        assertThat(result.get().getQuantity())
                .isEqualTo(3);
    }

    @Test
    void findByCartIdAndProductId_shouldReturnEmpty_whenItemDoesNotExist() {
        var result = cartItemRepository.findByCartIdAndProductId(
                cart.getId(),
                product.getId()
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndCartId_shouldReturnCartItem_whenCartItemBelongsToCart() {
        CartItem cartItem = new CartItem(product, 5);
        cart.addItem(cartItem);

        cartItemRepository.save(cartItem);

        var result = cartItemRepository.findByIdAndCartId(
                cartItem.getId(),
                cart.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(cartItem.getId());
        assertThat(result.get().getCart().getId())
                .isEqualTo(cart.getId());
        assertThat(result.get().getProduct().getId())
                .isEqualTo(product.getId());
        assertThat(result.get().getQuantity())
                .isEqualTo(5);
    }

    @Test
    void findByIdAndCartId_shouldReturnEmpty_whenCartItemBelongsToDifferentCart() {
        CartItem cartItem = new CartItem(product, 2);
        cart.addItem(cartItem);

        cartItemRepository.save(cartItem);

        User anotherUser = new User();
        anotherUser.setName(
                "Another Cart User " + System.nanoTime()
        );
        anotherUser.setEmail(
                "another-cart-user-" + System.nanoTime() + "@test.com"
        );
        anotherUser.setPassword("encoded-password");

        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow();

        anotherUser.setRole(customerRole);
        anotherUser = userRepository.save(anotherUser);

        Cart anotherCart = new Cart(anotherUser);
        anotherCart = cartRepository.save(anotherCart);

        var result = cartItemRepository.findByIdAndCartId(
                cartItem.getId(),
                anotherCart.getId()
        );

        assertThat(result).isEmpty();
    }
}