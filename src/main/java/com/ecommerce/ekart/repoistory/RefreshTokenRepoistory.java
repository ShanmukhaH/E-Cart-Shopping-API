package com.ecommerce.ekart.repoistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.RefreshToken;

public interface RefreshTokenRepoistory extends JpaRepository<RefreshToken, Long> {

	List<RefreshToken> findAllByExpirationBefore(LocalDateTime currentTime);

	Optional<RefreshToken > findByToken(String rt);
}
