package com.ecommerce.ekart.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.ekart.entity.Customer;
import com.ecommerce.ekart.entity.Seller;
import com.ecommerce.ekart.entity.User;
import com.ecommerce.ekart.exception.DataAlreadyPresentException;
import com.ecommerce.ekart.exception.UserAlreadyExistByEmailException;
import com.ecommerce.ekart.repoistory.CustomerRepoistory;
import com.ecommerce.ekart.repoistory.SellerRepoistory;
import com.ecommerce.ekart.repoistory.UserRepoistory;
import com.ecommerce.ekart.requestdto.UserRequest;
import com.ecommerce.ekart.responsedto.UserResponse;
import com.ecommerce.ekart.service.AuthService;
import com.ecommerce.ekart.utility.ResponseStrcture;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {


	private SellerRepoistory sellerRepoistory;
	private CustomerRepoistory customerRepoistory;
	private UserRepoistory userRepoistory;
	private ResponseStrcture<UserResponse> strcture;


	private <T extends User> T mapToUser(UserRequest userRequest){
		User user=null;
		switch (userRequest.getUserRole()){
		case CUSTOMER->{user=new Customer();}
		case SELLER->{user=new Seller();}
		default->
		throw new IllegalArgumentException("Unexpected value: " + user.getUserRole());

		}
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
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


		User user = userRepoistory.findByusername(userrequest.getEmail().split("@")[0]).map(user1->{

			if(user1.isEmailVerfied()) throw new UserAlreadyExistByEmailException("User Already Present");

			else {
				// Send otp to email
				// under maintance
			}
			return user1;
		}).orElseGet(()->saveUser(userrequest));

		return new  ResponseEntity<ResponseStrcture<UserResponse>>(strcture.setStatus(HttpStatus.OK.value())
				.setMessage("User registred Successfully,Please Varify your email by OTP")
				.setData(mapToUserResponse(user)),HttpStatus.OK);
	}

	@Override
	public void cleanupnonVerfiedUser() {

		List<User> list = userRepoistory.findByIsEmailVerfiedFalse();

		if(!list.isEmpty())
		{
			list.forEach(user->userRepoistory.delete(user));
		}

	}

}

