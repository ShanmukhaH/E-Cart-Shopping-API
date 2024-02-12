package com.ecommerce.ekart.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.ekart.cache.CacheStore;
import com.ecommerce.ekart.entity.AccessToken;
import com.ecommerce.ekart.entity.Customer;
import com.ecommerce.ekart.entity.RefreshToken;
import com.ecommerce.ekart.entity.Seller;
import com.ecommerce.ekart.entity.User;
import com.ecommerce.ekart.exception.InvalidOTPException;
import com.ecommerce.ekart.exception.OTPExcpiredException;
import com.ecommerce.ekart.exception.UserAlreadyExistByEmailException;
import com.ecommerce.ekart.exception.UserNotLoggedInException;
import com.ecommerce.ekart.exception.UserSessionExpiredException;
import com.ecommerce.ekart.repoistory.AccessTokenRepoistory;
import com.ecommerce.ekart.repoistory.CustomerRepoistory;
import com.ecommerce.ekart.repoistory.RefreshTokenRepoistory;
import com.ecommerce.ekart.repoistory.SellerRepoistory;
import com.ecommerce.ekart.repoistory.UserRepoistory;
import com.ecommerce.ekart.requestdto.AuthRequest;
import com.ecommerce.ekart.requestdto.OtpModel;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.AuthResponse;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.security.JwtService;
import com.ecommerce.ekart.service.AuthService;
import com.ecommerce.ekart.utility.CookieManager;
import com.ecommerce.ekart.utility.MessageStructure;
import com.ecommerce.ekart.utility.ResponseStrcture;
import com.ecommerce.ekart.utility.SimpleResponseStrcture;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


	private SellerRepoistory sellerRepoistory;

	private CustomerRepoistory customerRepoistory;

	private UserRepoistory userRepoistory;

	private ResponseStrcture<UserResponse> strcture;

	private ResponseStrcture<AuthResponse> authstrcture;

//	private SimpleResponseStrcture<AuthResponse> sauthStrcture;

	private PasswordEncoder encoder;

	private CacheStore<String> otpcachestore;

	private CacheStore<User>  usercacheStore;

	private JavaMailSender javaMailSender;

	private AuthenticationManager authenticationManager;

	private CookieManager cookieManager;

	private JwtService jwtService;

	private AccessTokenRepoistory accessTokenRepoistory;

	private RefreshTokenRepoistory refreshTokenRepoistory;

	@Value("${myapp.access.expiry}")
	private int acessExpiryInSeconds;

	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;


	public AuthServiceImpl(SellerRepoistory sellerRepoistory, CustomerRepoistory customerRepoistory,
			UserRepoistory userRepoistory, ResponseStrcture<UserResponse> strcture,
			ResponseStrcture<AuthResponse> authstrcture, PasswordEncoder encoder, CacheStore<String> otpcachestore,
			CacheStore<User> usercacheStore, JavaMailSender javaMailSender, AuthenticationManager authenticationManager,
			CookieManager cookieManager, JwtService jwtService, AccessTokenRepoistory accessTokenRepoistory,
			RefreshTokenRepoistory refreshTokenRepoistory) {
		super();
		this.sellerRepoistory = sellerRepoistory;
		this.customerRepoistory = customerRepoistory;
		this.userRepoistory = userRepoistory;
		this.strcture = strcture;
		this.authstrcture = authstrcture;
		this.encoder = encoder;
		this.otpcachestore = otpcachestore;
		this.usercacheStore = usercacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepoistory = accessTokenRepoistory;
		this.refreshTokenRepoistory = refreshTokenRepoistory;
//		this.sauthStrcture=sauthStrcture;

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
			log.error("The email address doesn't exist");
		}
		return new  ResponseEntity<ResponseStrcture<UserResponse>>(strcture.setStatus(HttpStatus.ACCEPTED.value())
				.setMessage("Please Verfiy Throug OTP sent To your mail Id: "+OTP)		
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
	public void cleanupExpiredRefreshTokens() {

		List<RefreshToken> refreshToken = refreshTokenRepoistory.findAllByExpirationBefore(LocalDateTime.now());
		refreshTokenRepoistory.deleteAll();
	}

	@Override
	public void cleanupExpiredAccessTokens() {
		List<AccessToken> acessToken = accessTokenRepoistory.findAllByExpirationBefore(LocalDateTime.now());
		accessTokenRepoistory.deleteAll();

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

		try {
			sendRegistrationSucessMail(user);
		} catch (MessagingException e) {
			log.error("Connection Failed");
		}

		return new ResponseEntity<ResponseStrcture<UserResponse>>(strcture.setStatus(HttpStatus.OK.value())
				.setMessage("Verfied OTp SUcessfully")
				.setData(mapToUserResponse(user)),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ResponseStrcture<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {

		String username=authRequest.getEmail().split("@")[0];
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username,authRequest.getPassword());
		Authentication authenticate = authenticationManager.authenticate(token);
		if(!authenticate.isAuthenticated())
			throw new UsernameNotFoundException("Failed TO authenticate User");
		else 
			//Generating the cookies and AuthResponse and returning to the client.
			return userRepoistory.findByusername(username).map(user ->{
				grantAcess(response, user);
				return ResponseEntity.ok(authstrcture.setStatus(HttpStatus.OK.value())
						.setData(AuthResponse.builder()
								.userId(user.getUserId())
								.username(username)
								.role(user.getUserRole().name())
								.isAuthenticated(true)
								.accessExpiraion(LocalDateTime.now().plusSeconds(acessExpiryInSeconds))
								.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
								.build())
						.setMessage(""));
			}).get();
	}


	@Override
	public ResponseEntity<SimpleResponseStrcture> logout( String refreshToken, String accesstoken,HttpServletResponse response) {


		if(accesstoken==null&&refreshToken==null) {
			throw new UserNotLoggedInException("Please Login");
		}
		accessTokenRepoistory.findByToken(accesstoken).ifPresent(accessToken ->{
			accessToken.setIsblocked(true);
			accessTokenRepoistory.save(accessToken);
		});
		refreshTokenRepoistory.findByToken(refreshToken).ifPresent(refreshtoken ->{
			refreshtoken.setIsblocked(true);
			refreshTokenRepoistory.save(refreshtoken);
		});
		response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));

		SimpleResponseStrcture strcture=new SimpleResponseStrcture();
		strcture.setStatus(HttpStatus.OK.value());
		strcture.setMessage("Logout Sucesfully!!!");
		return new ResponseEntity<SimpleResponseStrcture>(strcture,HttpStatus.OK);

	}


	@Override
	public ResponseEntity<SimpleResponseStrcture> revokeOther(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if(username!=null) {
			userRepoistory.findByusername(username)
			.ifPresent(user->{
				blockedAcessTokens(accessTokenRepoistory.findAllByUserAndIsblockedAndTokenNot(user, false, accessToken));
				blockedRefreshTokens(refreshTokenRepoistory.findAllByUserAndIsblockedAndTokenNot(user, false, refreshToken));
			});
			
			SimpleResponseStrcture strcture=new SimpleResponseStrcture();
			strcture.setStatus(HttpStatus.OK.value());
			strcture.setMessage("Logged out from all other device");
			return new ResponseEntity<SimpleResponseStrcture>(strcture,HttpStatus.OK);
		}
		 throw new IllegalArgumentException("User Not Authenticated") ;
	}


	private void blockedAcessTokens(List<AccessToken> acessTokens) {
		acessTokens.forEach(at->{
			at.setIsblocked(true);
			accessTokenRepoistory.save(at);
		});
	}

	private void blockedRefreshTokens(List<RefreshToken> refreshTokens) {
		refreshTokens.forEach(rt->{
			rt.setIsblocked(true);
			refreshTokenRepoistory.save(rt);
		});
	}



	private void grantAcess(HttpServletResponse response,User user) {
		//generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUsername());
		String refreshToken = jwtService.generaterefreshToken(user.getUsername());

		//adding access and refresh tokens cookies to the response
		response.addCookie(cookieManager.configure(new Cookie("at", refreshToken),acessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken),refreshExpiryInSeconds));

		//saving the access and refresh cookie into database
		accessTokenRepoistory.save(AccessToken.builder()
				.token(accessToken)
				.isblocked(false)
				.expiration(LocalDateTime.now().plusSeconds(acessExpiryInSeconds))
				.build());

		refreshTokenRepoistory.save(RefreshToken.builder()
				.token(refreshToken)
				.isblocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build());
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







}

