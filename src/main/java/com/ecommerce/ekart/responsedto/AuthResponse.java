package com.ecommerce.ekart.responsedto;

import java.time.LocalDateTime;

import com.ecommerce.ekart.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

	private int userId;
	private String username;
	private String role;
	private boolean isAuthenticated;
	private LocalDateTime accessExpiraion;
	private LocalDateTime refreshExpiration;
	
	
}
