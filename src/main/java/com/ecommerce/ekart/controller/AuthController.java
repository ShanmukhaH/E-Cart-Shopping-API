package com.ecommerce.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.ekart.requestdto.OtpModel;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.service.AuthService;
import com.ecommerce.ekart.utility.ResponseStrcture;

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
	
}
