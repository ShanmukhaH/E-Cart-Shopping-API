package com.ecommerce.ekart.utility;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class SimpleResponseStrcture<T> {


	private int status;
	private String message;
	
	public SimpleResponseStrcture<T> setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public SimpleResponseStrcture<T> setMessage(String message) {
		this.message = message;
		return this;
	}
	
}
