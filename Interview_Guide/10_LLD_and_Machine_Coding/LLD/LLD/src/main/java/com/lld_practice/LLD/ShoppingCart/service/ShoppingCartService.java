package com.lld_practice.LLD.ShoppingCart.service;

import com.lld_practice.LLD.ShoppingCart.dto.CreateCartItemRequest;
import com.lld_practice.LLD.ShoppingCart.dto.CreateCustomerRequest;
import com.lld_practice.LLD.ShoppingCart.dto.CreateProductRequest;
import com.lld_practice.LLD.ShoppingCart.entity.Cart;
import com.lld_practice.LLD.ShoppingCart.entity.CartItem;
import com.lld_practice.LLD.ShoppingCart.entity.Customer;
import com.lld_practice.LLD.ShoppingCart.entity.Product;
import com.lld_practice.LLD.ShoppingCart.repository.CartItemRepository;
import com.lld_practice.LLD.ShoppingCart.repository.CartRepository;
import com.lld_practice.LLD.ShoppingCart.repository.CustomerRepository;
import com.lld_practice.LLD.ShoppingCart.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ShoppingCartService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public Customer addCustomer(CreateCustomerRequest request) {

        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setPhoneNumber(request.getPhoneNumber());

        return customerRepository.save(customer);
    }

    public Product addProduct(CreateProductRequest request) {

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        return productRepository.save(product);
    }

    public CartItem addProductToCart(CreateCartItemRequest request) {
        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getQuantity() > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product: "
                            + product.getProductName());
        }
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        BigDecimal totalPrice = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));
        item.setTotalPrice(totalPrice);
        return cartItemRepository.save(item);
    }

    public Cart addCartItemToCart(Integer customerId, CartItem item) {
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Cart cartCustomer = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        item.setCart(cartCustomer);
        cartCustomer.getCartItems().add(item);
        return cartRepository.save(cartCustomer);
    }

}