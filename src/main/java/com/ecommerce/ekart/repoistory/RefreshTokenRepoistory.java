package com.ecommerce.ekart.repoistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.RefreshToken;

public interface RefreshTokenRepoistory extends JpaRepository<RefreshToken, Long> {


	Optional<RefreshToken > findByToken(String rt);
}
