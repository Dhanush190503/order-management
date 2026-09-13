package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_shouldReturnRole_whenRoleExists() {
        Optional<Role> result =
                roleRepository.findByName(RoleName.CUSTOMER);

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo(RoleName.CUSTOMER);
    }

    @Test
    void findByName_shouldReturnEmpty_whenRoleDoesNotExist() {
        Optional<Role> result =
                roleRepository.findByName(null);

        assertThat(result).isEmpty();
    }
}