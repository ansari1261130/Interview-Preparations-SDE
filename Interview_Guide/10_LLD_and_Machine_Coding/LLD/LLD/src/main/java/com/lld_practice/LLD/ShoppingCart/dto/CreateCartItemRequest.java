package com.lld_practice.LLD.ShoppingCart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCartItemRequest {

    private Integer customerId;
    private Integer productId;
    private Integer quantity;
}