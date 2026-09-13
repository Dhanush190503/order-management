package com.ecommerce.ordermanagement.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String sku,
            BigDecimal price,
            Integer quantity,
            BigDecimal subtotal
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}