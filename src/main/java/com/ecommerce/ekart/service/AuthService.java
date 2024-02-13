package com.ecommerce.ekart.service;

import org.springframework.http.ResponseEntity;

import com.ecommerce.ekart.requestdto.AuthRequest;
import com.ecommerce.ekart.requestdto.OtpModel;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.AuthResponse;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.utility.ResponseStrcture;
import com.ecommerce.ekart.utility.SimpleResponseStrcture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStrcture<UserResponse>> registerUser(UserRequest userrequest);
	
	 void cleanupnonVerfiedUser();

	ResponseEntity<ResponseStrcture<UserResponse>> verfiyOTP(OtpModel otpModel);

	ResponseEntity<ResponseStrcture<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response,String accessToken, String refreshToken);

	ResponseEntity<SimpleResponseStrcture> logout(String refreshToken,String accesstoken,HttpServletResponse response);
	
	void cleanupExpiredRefreshTokens();
	void cleanupExpiredAccessTokens();

	ResponseEntity<SimpleResponseStrcture> revokeOther(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleResponseStrcture> revokeAll(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleResponseStrcture> refreshLogin(String accessToken, String refreshToken,
			HttpServletResponse response);

	
}
