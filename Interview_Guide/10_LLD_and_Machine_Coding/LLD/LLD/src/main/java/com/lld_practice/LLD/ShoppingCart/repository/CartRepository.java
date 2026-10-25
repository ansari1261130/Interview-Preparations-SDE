package com.lld_practice.LLD.ShoppingCart.repository;

import com.lld_practice.LLD.ShoppingCart.entity.Cart;
import com.lld_practice.LLD.ShoppingCart.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByCustomer(Customer customer);
}
