package com.ecommerce.ekart.responsedto;

import com.ecommerce.ekart.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

	private int userId;
	private String username;
	private String email;
	private UserRole userRole;
	private boolean isEmailVerfied;
	private boolean isDeleted;
}
