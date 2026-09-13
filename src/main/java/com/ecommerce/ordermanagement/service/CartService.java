package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.AddCartItemRequest;
import com.ecommerce.ordermanagement.dto.CartItemResponse;
import com.ecommerce.ordermanagement.dto.CartResponse;
import com.ecommerce.ordermanagement.dto.UpdateCartItemRequest;
import com.ecommerce.ordermanagement.entity.Cart;
import com.ecommerce.ordermanagement.entity.CartItem;
import com.ecommerce.ordermanagement.entity.Product;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.CartItemRepository;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartResponse getCart(String email) {

        User user = getUserByEmail(email);

        Cart cart = getOrCreateCart(user);

        return mapToCartResponse(cart);
    }

    public CartResponse addItem(
            String email,
            AddCartItemRequest request
    ) {

        User user = getUserByEmail(email);

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Product not found with id: "
                                + request.getProductId()
                )
        );

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        int newQuantity;

        if (cartItem == null) {

            newQuantity = request.getQuantity();

            cartItem = new CartItem(
                    product,
                    newQuantity
            );

            cart.addItem(cartItem);

        } else {

            newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();

            cartItem.setQuantity(newQuantity);
        }

        validateStock(product, newQuantity);

        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    public CartResponse updateItem(
            String email,
            Long itemId,
            UpdateCartItemRequest request
    ) {

        User user = getUserByEmail(email);

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: "
                                        + itemId
                        )
                );

        validateStock(
                cartItem.getProduct(),
                request.getQuantity()
        );

        cartItem.setQuantity(
                request.getQuantity()
        );

        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    public void removeItem(
            String email,
            Long itemId
    ) {

        User user = getUserByEmail(email);

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: "
                                        + itemId
                        )
                );

        cart.removeItem(cartItem);

        cartItemRepository.delete(cartItem);
    }

    private User getUserByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Authenticated user email is required"
            );
        }

        return userRepository.findByEmail(email.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + email
                        )
                );
    }

    private Cart getOrCreateCart(User user) {

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {

                    Cart newCart = new Cart(user);

                    return cartRepository.save(newCart);
                });
    }

    private void validateStock(
            Product product,
            int requestedQuantity
    ) {

        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        if (requestedQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + ". Available stock: "
                            + product.getStockQuantity()
            );
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                items,
                totalPrice
        );
    }

    private CartItemResponse mapToCartItemResponse(
            CartItem cartItem
    ) {

        Product product = cartItem.getProduct();

        BigDecimal subtotal =
                product.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        cartItem.getQuantity()
                                )
                        );

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}