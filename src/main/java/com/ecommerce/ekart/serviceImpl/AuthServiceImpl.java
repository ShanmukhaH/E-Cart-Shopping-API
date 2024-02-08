package com.ecommerce.ekart.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.ekart.cache.CacheStore;
import com.ecommerce.ekart.entity.Customer;
import com.ecommerce.ekart.entity.Seller;
import com.ecommerce.ekart.entity.User;
import com.ecommerce.ekart.exception.DataAlreadyPresentException;
import com.ecommerce.ekart.exception.InvalidOTPException;
import com.ecommerce.ekart.exception.OTPExcpiredException;
import com.ecommerce.ekart.exception.UserAlreadyExistByEmailException;
import com.ecommerce.ekart.exception.UserSessionExpiredException;
import com.ecommerce.ekart.repoistory.CustomerRepoistory;
import com.ecommerce.ekart.repoistory.SellerRepoistory;
import com.ecommerce.ekart.repoistory.UserRepoistory;
import com.ecommerce.ekart.requestdto.OtpModel;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.service.AuthService;
import com.ecommerce.ekart.utility.MessageStructure;
import com.ecommerce.ekart.utility.ResponseStrcture;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {


	private SellerRepoistory sellerRepoistory;
	private CustomerRepoistory customerRepoistory;
	private UserRepoistory userRepoistory;
	private ResponseStrcture<UserResponse> strcture;
	private PasswordEncoder encoder;
	private CacheStore<String> otpcachestore;
	private CacheStore<User>  usercacheStore;
	private JavaMailSender javaMailSender;



	private <T extends User> T mapToUser(UserRequest userRequest){
		User user=null;
		switch (userRequest.getUserRole()){
		case CUSTOMER->{user=new Customer();}
		case SELLER->{user=new Seller();}
		default->
		throw new IllegalArgumentException("Unexpected value: " + user.getUserRole());

		}
		user.setEmail(userRequest.getEmail());
		user.setPassword(encoder.encode(userRequest.getPassword()));
		user.setUsername(user.getEmail().split("@")[0]);
		user.setUserRole(userRequest.getUserRole());
		return (T) user;	
	}

	private <T extends UserResponse> T mapToUserResponse(User user) {
		return (T) new UserResponse().builder().
				userId(user.getUserId()).
				username(user.getUsername()).
				email(user.getEmail()).
				userRole(user.getUserRole()).
				isEmailVerfied(user.isEmailVerfied()).
				isDeleted(user.isDeleted()).build();
	}

	private User saveUser(UserRequest userRequest) {

		User user = mapToUser(userRequest);
		switch (user.getUserRole()) {

		case CUSTOMER-> {
			user=customerRepoistory.save((Customer)user);
		}
		case SELLER->{
			user=sellerRepoistory.save((Seller)user);
		}
		default->
		throw new IllegalArgumentException("Unexpected value: " + user.getUserRole());
		}
		return user;
	}


	@Override
	public ResponseEntity<ResponseStrcture<UserResponse>> registerUser(UserRequest userrequest) {

		if(userRepoistory.existsByemail(userrequest.getEmail()))
			throw new UserAlreadyExistByEmailException("User already is Present With given Email id");

		String OTP=generateOtp();
		User user=mapToUser(userrequest);
		usercacheStore.add(userrequest.getEmail(), user);
		otpcachestore.add(userrequest.getEmail(), OTP);
		
		try {
			sendOtpToMail(user, OTP);
		} catch (MessagingException ex) {
			log.error("The email adress dosent exists");
		}
		


		return new  ResponseEntity<ResponseStrcture<UserResponse>>(strcture.setStatus(HttpStatus.ACCEPTED.value())
				.setMessage("Please Verfiy Throug OTP sent To your mail Id")		
				.setData(mapToUserResponse(user)),HttpStatus.ACCEPTED);
	}

	@Override
	public void cleanupnonVerfiedUser() {

		List<User> list = userRepoistory.findByIsEmailVerfiedFalse();

		if(!list.isEmpty())
		{
			list.forEach(user->userRepoistory.delete(user));
		}

	}

	@Override
	public ResponseEntity<ResponseStrcture<UserResponse>> verfiyOTP(@RequestBody OtpModel otpModel) {
		User user=usercacheStore.get(otpModel.getEmail());
		String otp=otpcachestore.get(otpModel.getEmail());

		if(otp==null) throw new OTPExcpiredException("OTP is Expired");
		if(user==null) throw new UserSessionExpiredException("Session Expired");
		if(!otp.equals(otpModel.getOtp())) throw new InvalidOTPException("Invalid OTp");

		user.setEmailVerfied(true);
		userRepoistory.save(user);
		return new ResponseEntity<ResponseStrcture<UserResponse>>(strcture.setStatus(HttpStatus.OK.value())
				.setMessage("Verfied OTp SUcessfully")
				.setData(mapToUserResponse(user)),HttpStatus.OK);
	}

	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mimeMessage=javaMailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setText(message.getText(),true);
		javaMailSender.send(mimeMessage);

	}
	private void sendOtpToMail(User user,String Otp) throws MessagingException {
		
		sendMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Complte Your Registation To Ekart ")
				.sentDate(new Date())
				.text(
						"hey "+user.getUsername()
						+ "Good to see you intrested in E-kart<br>"
						+ "<h1>"+Otp+"</h1><br>"
						+ "Note: OTP expires in 1 minute"
						+"<br><br>"
						+"with best regards <br>"
						+"E-kart")
				.build());
		
	}
	
	private void sendRegistrationSucessMail(User user) throws MessagingException {
		
		sendMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Registration Sucessfull-Ekart")
				.sentDate(new Date())
				.text(
						"hey Welcome buddy "+user.getUsername()
						+ "User Registred Sucessfully"
						+"<br><br>"
						+"with best regards <br>"
						+"E-kart")
				.build());
	}
	private String generateOtp() {
		return String.valueOf(new Random().nextInt(100000, 999999));

	}
}

