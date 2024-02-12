package com.ecommerce.ekart.exceptionhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ecommerce.ekart.exception.DataAlreadyPresentException;
import com.ecommerce.ekart.exception.InvalidOTPException;
import com.ecommerce.ekart.exception.OTPExcpiredException;
import com.ecommerce.ekart.exception.UserAlreadyExistByEmailException;
import com.ecommerce.ekart.exception.UserNotLoggedInException;
import com.ecommerce.ekart.exception.UserSessionExpiredException;

public class AuthExceptionHandler extends ResponseEntityExceptionHandler{

	private ResponseEntity<Object> structure(HttpStatus status,String message,Object rootcause){
		return new ResponseEntity<Object>(Map.of(
				"status",status.value(),
				"message",message,
				"rootcause",rootcause),status
				);
	}
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<ObjectError> allErrors = ex.getAllErrors();

		Map<String, String> errors=new HashMap();

		allErrors.forEach(error->{
			FieldError fieldError=(FieldError)error; 
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		});

		return structure(HttpStatus.BAD_REQUEST, "Failed to Save the data", errors);
	}
	@ExceptionHandler(DataAlreadyPresentException.class)
	public ResponseEntity<Object> handleDataAlradyPresentException(DataAlreadyPresentException ex){
		return structure(HttpStatus.FOUND, ex.getMessage(), "Data Alrady Present");
	}

	@ExceptionHandler(UserAlreadyExistByEmailException.class)
	public ResponseEntity<Object> handleUserAlreadyPresent(UserAlreadyExistByEmailException ex){
		return structure(HttpStatus.FOUND, ex.getMessage(), "User ALready pResent");
	}
	
	@ExceptionHandler(OTPExcpiredException.class)
	public ResponseEntity<Object> handleOTPExpiredException(OTPExcpiredException ex){
		return structure(HttpStatus.BAD_REQUEST, ex.getMessage(), "OTP is Expired");
	}
	
	@ExceptionHandler(UserSessionExpiredException.class)
	public ResponseEntity<Object> handelUserSessionExpiredException(UserSessionExpiredException ex){
		return structure(HttpStatus.BAD_REQUEST, ex.getMessage(), "User Session Expired");
	}
	
	@ExceptionHandler(InvalidOTPException.class)
	public ResponseEntity<Object> handleInvalidOtpException(InvalidOTPException ex){
		return structure(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid Otp");
	}
	
	@ExceptionHandler(UserNotLoggedInException.class)
	ResponseEntity<Object> handleUserNotLoogedInException(UserNotLoggedInException ex){
		return structure(HttpStatus.BAD_REQUEST, ex.getMessage(), "Plese Login");
	}
}
