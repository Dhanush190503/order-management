package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Cart;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new IllegalStateException("CUSTOMER role not found")
                );

        user = new User();
        user.setName("Cart Repository User " + System.nanoTime());
        user.setEmail(
                "cart-repository-" + System.nanoTime() + "@test.com"
        );
        user.setPassword("encoded-password");
        user.setRole(customerRole);

        user = userRepository.save(user);
    }

    @Test
    void findByUserId_shouldReturnCart_whenUserHasCart() {
        Cart cart = new Cart(user);
        cart = cartRepository.save(cart);

        Optional<Cart> result =
                cartRepository.findByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(cart.getId());
        assertThat(result.get().getUser().getId())
                .isEqualTo(user.getId());
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenUserHasNoCart() {
        Optional<Cart> result =
                cartRepository.findByUserId(user.getId());

        assertThat(result).isEmpty();
    }
}