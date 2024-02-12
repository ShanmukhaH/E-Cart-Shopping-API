package com.ecommerce.ekart.repoistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.AccessToken;
import com.ecommerce.ekart.entity.User;

public interface AccessTokenRepoistory extends JpaRepository<AccessToken, Long> {

	Optional<AccessToken> findByToken(String at);

	List<AccessToken> findAllByExpirationBefore(LocalDateTime currentTime);

	Optional<AccessToken> findByTokenAndIsblocked(String token, boolean isBlocked );
	
	 List<AccessToken>  findAllByUserAndIsblocked(User user,boolean b);
	 
	List<AccessToken>  findAllByUserAndIsblockedAndTokenNot(User user,boolean b,String tooken);
}
