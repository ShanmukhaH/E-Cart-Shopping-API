package com.ecommerce.ekart.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.ekart.entity.User;

@Configuration
public class CacheBeanConfig {

	@Bean
	public CacheStore<User> UsercacheStore(){
		return new CacheStore<User>(Duration.ofMinutes(5));
	}
	
	@Bean
	public CacheStore<String> OtpCacheStore(){
		return new CacheStore<String>(Duration.ofMinutes(5));
	}
}
