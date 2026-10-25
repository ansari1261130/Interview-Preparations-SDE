package com.lld_practice.LLD.ShoppingCart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    private String productName;
    private BigDecimal price;
    private Integer stockQuantity;
}
