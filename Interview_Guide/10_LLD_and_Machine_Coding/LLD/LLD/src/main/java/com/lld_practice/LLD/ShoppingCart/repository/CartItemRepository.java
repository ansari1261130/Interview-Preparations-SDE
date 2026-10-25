package com.lld_practice.LLD.ShoppingCart.repository;

import com.lld_practice.LLD.ShoppingCart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
