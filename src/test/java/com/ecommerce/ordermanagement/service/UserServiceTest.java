package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.UserResponse;
import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {

        UserRepository userRepository =
                mock(UserRepository.class);

        UserService userService =
                new UserService(userRepository);

        Role customerRole =
                new Role(RoleName.CUSTOMER);

        User user =
                new User(
                        "Customer",
                        "customer@example.com",
                        "encoded-password",
                        customerRole
                );

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserById(3L);

        assertEquals(
                "Customer",
                response.getName()
        );

        assertEquals(
                "customer@example.com",
                response.getEmail()
        );

        assertEquals(
                "CUSTOMER",
                response.getRole()
        );

        verify(userRepository)
                .findById(3L);
    }


    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        UserRepository userRepository =
                mock(UserRepository.class);

        UserService userService =
                new UserService(userRepository);

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(999L)
        );

        verify(userRepository)
                .findById(999L);
    }


    @Test
    void getAllUsers_shouldSearchByName_whenSearchIsProvided() {

        UserRepository userRepository =
                mock(UserRepository.class);

        UserService userService =
                new UserService(userRepository);

        Role customerRole =
                new Role(RoleName.CUSTOMER);

        User user =
                new User(
                        "Customer",
                        "customer@example.com",
                        "encoded-password",
                        customerRole
                );

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user)
                );

        Pageable pageable =
                PageRequest.of(0, 10);

        when(userRepository.findByNameContainingIgnoreCase(
                "customer",
                pageable
        )).thenReturn(userPage);

        Page<UserResponse> response =
                userService.getAllUsers(
                        "customer",
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "Customer",
                response.getContent()
                        .get(0)
                        .getName()
        );

        assertEquals(
                "customer@example.com",
                response.getContent()
                        .get(0)
                        .getEmail()
        );

        assertEquals(
                "CUSTOMER",
                response.getContent()
                        .get(0)
                        .getRole()
        );

        verify(userRepository)
                .findByNameContainingIgnoreCase(
                        "customer",
                        pageable
                );
    }


    @Test
    void getAllUsers_shouldReturnAllUsers_whenSearchIsEmpty() {

        UserRepository userRepository =
                mock(UserRepository.class);

        UserService userService =
                new UserService(userRepository);

        Role customerRole =
                new Role(RoleName.CUSTOMER);

        User user =
                new User(
                        "Customer",
                        "customer@example.com",
                        "encoded-password",
                        customerRole
                );

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user)
                );

        Pageable pageable =
                PageRequest.of(0, 10);

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        Page<UserResponse> response =
                userService.getAllUsers(
                        "",
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "Customer",
                response.getContent()
                        .get(0)
                        .getName()
        );

        assertEquals(
                "customer@example.com",
                response.getContent()
                        .get(0)
                        .getEmail()
        );

        assertEquals(
                "CUSTOMER",
                response.getContent()
                        .get(0)
                        .getRole()
        );

        verify(userRepository)
                .findAll(pageable);
    }
}