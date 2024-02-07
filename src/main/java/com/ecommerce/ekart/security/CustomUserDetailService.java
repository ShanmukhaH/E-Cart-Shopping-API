package com.ecommerce.ekart.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.ekart.repoistory.UserRepoistory;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

	private UserRepoistory userRepoistory;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		return userRepoistory.findByusername(username).map(user->new CustomUserDetail(user)).orElseThrow(()->new UsernameNotFoundException("Failed to authenticate user"));
	}

}
