package com.ecommerce.ekart.utility;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class SimpleResponseStrcture {


	private int status;
	private String message;
	
	public SimpleResponseStrcture setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public SimpleResponseStrcture  setMessage(String message) {
		this.message = message;
		return this;
	}
	
}
