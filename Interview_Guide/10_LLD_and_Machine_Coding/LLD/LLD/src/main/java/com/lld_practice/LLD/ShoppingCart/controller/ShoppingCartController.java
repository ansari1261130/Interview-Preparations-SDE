package com.lld_practice.LLD.ShoppingCart.controller;

import com.lld_practice.LLD.ShoppingCart.dto.CreateCartItemRequest;
import com.lld_practice.LLD.ShoppingCart.dto.CreateCustomerRequest;
import com.lld_practice.LLD.ShoppingCart.dto.CreateProductRequest;
import com.lld_practice.LLD.ShoppingCart.entity.CartItem;
import com.lld_practice.LLD.ShoppingCart.entity.Product;
import com.lld_practice.LLD.ShoppingCart.service.ShoppingCartService;
import com.lld_practice.LLD.ShoppingCart.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;


    @PostMapping("/addCustomer")
    public ResponseEntity<Customer> addCustomer(
            @RequestBody CreateCustomerRequest request) {
        Customer customer = shoppingCartService.addCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<Product> addProduct(
            @RequestBody CreateProductRequest request) {
        Product product = shoppingCartService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PostMapping("/addProductToCart")
    public ResponseEntity<CartItem> addProductToCart(
            @RequestBody CreateCartItemRequest request) {
        CartItem items = shoppingCartService.addProductToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(items);
    }

    @PostMapping("/addCartItemToCart")
    public ResponseEntity<CartItem> addCartItemToCart(
            @RequestBody CreateCartItemRequest request) {
        CartItem items = shoppingCartService.addProductToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(items);
    }

}
