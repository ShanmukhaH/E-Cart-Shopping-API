package com.ecommerce.ekart.utility;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStrcture<T> {

	private int status;
	private String message;
	private T data;
	public int getStatus() {
		return status;
	}
	public ResponseStrcture<T> setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public ResponseStrcture<T> setMessage(String message) {
		this.message = message;
		return this;
	}
	public T getData() {
		return data;
	}
	public ResponseStrcture<T> setData(T data) {
		this.data = data;
		return this;
	}
}
