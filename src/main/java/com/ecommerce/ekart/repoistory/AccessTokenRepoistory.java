package com.ecommerce.ekart.repoistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.AccessToken;

public interface AccessTokenRepoistory extends JpaRepository<AccessToken, Long> {

	Optional<AccessToken> findByToken(String at);

}
