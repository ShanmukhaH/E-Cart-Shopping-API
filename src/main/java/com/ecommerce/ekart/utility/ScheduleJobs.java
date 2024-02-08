package com.ecommerce.ekart.utility;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.ekart.service.AuthService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduleJobs {

	private AuthService authService;
	
	@Scheduled(fixedDelay = 1000l*60*8)
	public void cleanupnonverfiedUser() {
		authService.cleanupnonVerfiedUser();
	}
}
