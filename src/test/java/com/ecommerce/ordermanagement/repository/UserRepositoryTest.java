package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.CUSTOMER);
                    return roleRepository.save(role);
                });
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User user = new User();
        user.setName("Repository Test User");
        user.setEmail("repository@test.com");
        user.setPassword("encoded-password");
        user.setRole(customerRole);

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("repository@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("repository@test.com");
        assertThat(result.get().getName())
                .isEqualTo("Repository Test User");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> result =
                userRepository.findByEmail("does-not-exist@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        User user = new User();
        user.setName("Existing User");
        user.setEmail("exists@test.com");
        user.setPassword("encoded-password");
        user.setRole(customerRole);

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("exists@test.com"))
                .isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("missing@test.com"))
                .isFalse();
    }

    @Test
    void findByNameContainingIgnoreCase_shouldFindMatchingUsers() {
        User user1 = new User();
        user1.setName("John Repository");
        user1.setEmail("john-repository@test.com");
        user1.setPassword("password");
        user1.setRole(customerRole);

        User user2 = new User();
        user2.setName("Jane Repository");
        user2.setEmail("jane-repository@test.com");
        user2.setPassword("password");
        user2.setRole(customerRole);

        userRepository.save(user1);
        userRepository.save(user2);

        var result = userRepository.findByNameContainingIgnoreCase(
                "repository",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder(
                        "John Repository",
                        "Jane Repository"
                );
    }
}