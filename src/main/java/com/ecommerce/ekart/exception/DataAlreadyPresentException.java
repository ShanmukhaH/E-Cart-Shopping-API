package com.ecommerce.ekart.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataAlreadyPresentException extends RuntimeException {

	private String message;
}
