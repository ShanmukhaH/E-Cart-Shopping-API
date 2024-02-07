package com.ecommerce.ekart.repoistory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.Seller;

public interface SellerRepoistory extends JpaRepository<Seller, Integer> {

}
