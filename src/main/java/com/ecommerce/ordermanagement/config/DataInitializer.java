package com.ecommerce.ordermanagement.config;

import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {

            if (roleRepository.findByName(RoleName.CUSTOMER).isEmpty()) {
                roleRepository.save(new Role(RoleName.CUSTOMER));
            }

            if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
                roleRepository.save(new Role(RoleName.ADMIN));
            }
        };
    }
}