package com.lld_practice.LLD.ShoppingCart.repository;
import com.lld_practice.LLD.ShoppingCart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Integer> {
}
