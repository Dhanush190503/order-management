package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.AddCartItemRequest;
import com.ecommerce.ordermanagement.dto.CartResponse;
import com.ecommerce.ordermanagement.dto.UpdateCartItemRequest;
import com.ecommerce.ordermanagement.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getCart(email)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.addItem(
                        email,
                        request
                )
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.updateItem(
                        email,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long itemId
    ) {

        String email = authentication.getName();

        cartService.removeItem(
                email,
                itemId
        );

        return ResponseEntity.noContent().build();
    }
}