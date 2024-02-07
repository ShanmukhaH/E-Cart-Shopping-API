package com.ecommerce.ekart.repoistory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.ekart.entity.User;

public interface UserRepoistory extends JpaRepository<User, Integer>{

	boolean existsByemail(String email);
   
	Optional<User>findByusername(String username);
	
	  List<User> findByIsEmailVerfiedFalse();

}
