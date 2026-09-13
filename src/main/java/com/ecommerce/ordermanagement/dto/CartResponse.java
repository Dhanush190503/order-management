package com.ecommerce.ordermanagement.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;

    public CartResponse() {
    }

    public CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            BigDecimal totalPrice
    ) {
        this.cartId = cartId;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public Long getCartId() {
        return cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}