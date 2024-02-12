package com.ecommerce.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.ekart.requestdto.AuthRequest;
import com.ecommerce.ekart.requestdto.OtpModel;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.AuthResponse;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.service.AuthService;
import com.ecommerce.ekart.utility.ResponseStrcture;
import com.ecommerce.ekart.utility.SimpleResponseStrcture;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class AuthController {

	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStrcture<UserResponse>> registerUser(@RequestBody UserRequest userrequest){
		return authService.registerUser(userrequest) ;
		
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStrcture<UserResponse>> verfiyOTP(@RequestBody OtpModel otpModel){
		return authService.verfiyOTP(otpModel);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStrcture<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse response){
		return authService.login(authRequest,response);
	}
	

	@PostMapping("/logout")
	public ResponseEntity<SimpleResponseStrcture<AuthResponse>> logout(@CookieValue(name="rt", required=false) String refreshToken,@CookieValue(name = "at",required = false) String accesstoken,HttpServletResponse response){
	return authService.logout(refreshToken,accesstoken,response);	
	}
	
	
}
