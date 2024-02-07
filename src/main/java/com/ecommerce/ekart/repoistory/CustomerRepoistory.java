package com.ecommerce.ekart.repoistory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.Customer;

public interface CustomerRepoistory extends JpaRepository<Customer, Integer> {

}
