package com.ecommerce.ekart.requestdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

	private String email;
	private String password;
	private com.ecommerce.ekart.enums.UserRole userRole;
}
