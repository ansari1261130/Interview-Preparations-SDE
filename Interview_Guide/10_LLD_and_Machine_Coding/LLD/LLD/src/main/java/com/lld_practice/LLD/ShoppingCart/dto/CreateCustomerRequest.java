package com.lld_practice.LLD.ShoppingCart.dto;
import lombok.Data;

@Data
public class CreateCustomerRequest {

    private String customerName;
    private Long phoneNumber;
}