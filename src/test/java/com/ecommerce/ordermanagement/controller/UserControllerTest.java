package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.UserResponse;
import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UserControllerTest {

    // ============================================================
    // TEST 33
    // Get user by ID -> 200 OK
    // ============================================================

    @Test
    void getUserById_shouldReturn200AndUser_whenUserExists() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        UserResponse userResponse =
                new UserResponse(
                        3L,
                        "Test Customer",
                        "customer@example.com",
                        "CUSTOMER"
                );

        when(userService.getUserById(3L))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response =
                userController.getUserById(3L);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                userResponse,
                response.getBody()
        );

        assertEquals(
                3L,
                response.getBody().getId()
        );

        assertEquals(
                "Test Customer",
                response.getBody().getName()
        );

        assertEquals(
                "customer@example.com",
                response.getBody().getEmail()
        );

        assertEquals(
                "CUSTOMER",
                response.getBody().getRole()
        );

        verify(userService)
                .getUserById(3L);

        verifyNoMoreInteractions(
                userService
        );
    }


    // ============================================================
    // TEST 34
    // Get user by ID -> User not found
    // ============================================================

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        when(userService.getUserById(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "User not found with id: 999"
                        )
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> userController.getUserById(999L)
        );

        verify(userService)
                .getUserById(999L);

        verifyNoMoreInteractions(
                userService
        );
    }


    // ============================================================
    // TEST 35
    // Search users -> 200 OK
    // ============================================================

    @Test
    void getAllUsers_shouldReturn200AndSearchResults_whenSearchIsProvided() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        UserResponse userResponse =
                new UserResponse(
                        3L,
                        "Test Customer",
                        "customer@example.com",
                        "CUSTOMER"
                );

        Page<UserResponse> page =
                new PageImpl<>(
                        List.of(userResponse),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "id"
                                )
                        ),
                        1
                );

        when(userService.getAllUsers(
                "customer",
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<UserResponse>> response =
                userController.getAllUsers(
                        "customer",
                        0,
                        10,
                        "id",
                        "asc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        assertEquals(
                1,
                response.getBody().getTotalElements()
        );

        assertEquals(
                "Test Customer",
                response.getBody()
                        .getContent()
                        .get(0)
                        .getName()
        );

        verify(userService)
                .getAllUsers(
                        "customer",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "id"
                                )
                        )
                );

        verifyNoMoreInteractions(
                userService
        );
    }


    // ============================================================
    // TEST 36
    // Empty search -> calls service with null/empty search
    // ============================================================

    @Test
    void getAllUsers_shouldReturn200_whenSearchIsEmpty() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        Page<UserResponse> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "id"
                                )
                        ),
                        0
                );

        when(userService.getAllUsers(
                "",
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<UserResponse>> response =
                userController.getAllUsers(
                        "",
                        0,
                        10,
                        "id",
                        "asc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        assertEquals(
                0,
                response.getBody().getTotalElements()
        );

        verify(userService)
                .getAllUsers(
                        "",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "id"
                                )
                        )
                );

        verifyNoMoreInteractions(
                userService
        );
    }


    // ============================================================
    // TEST 37
    // Invalid sort direction -> IllegalArgumentException
    // ============================================================

    @Test
    void getAllUsers_shouldThrowIllegalArgumentException_whenSortDirectionIsInvalid() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userController.getAllUsers(
                                null,
                                0,
                                10,
                                "id",
                                "invalid"
                        )
                );

        assertEquals(
                "Invalid sort direction. Use 'asc' or 'desc'",
                exception.getMessage()
        );

        verifyNoMoreInteractions(
                userService
        );
    }


    // ============================================================
    // TEST 38
    // Descending sort -> correctly creates Pageable
    // ============================================================

    @Test
    void getAllUsers_shouldUseDescendingSort_whenDirectionIsDesc() {

        UserService userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        Page<UserResponse> page =
                new PageImpl<>(
                        List.of()
                );

        when(userService.getAllUsers(
                null,
                PageRequest.of(
                        1,
                        5,
                        Sort.by(
                                Sort.Direction.DESC,
                                "name"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<UserResponse>> response =
                userController.getAllUsers(
                        null,
                        1,
                        5,
                        "name",
                        "desc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        verify(userService)
                .getAllUsers(
                        null,
                        PageRequest.of(
                                1,
                                5,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "name"
                                )
                        )
                );

        verifyNoMoreInteractions(
                userService
        );
    }
}