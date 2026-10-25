package com.lld_practice.LLD.ShoppingCart.repository;
import com.lld_practice.LLD.ShoppingCart.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}