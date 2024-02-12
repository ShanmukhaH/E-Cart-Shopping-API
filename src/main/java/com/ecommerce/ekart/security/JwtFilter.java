package com.ecommerce.ekart.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.ekart.entity.AccessToken;
import com.ecommerce.ekart.exception.InvalidAuthenticationException;
import com.ecommerce.ekart.exception.UserNotLoggedInException;
import com.ecommerce.ekart.repoistory.AccessTokenRepoistory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private AccessTokenRepoistory accessTokenRepoistory;
	
	private JwtService jwtService;
	
	private CustomUserDetailService customUserDetailService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
	
		String at=null;
		String rt=null;
		Cookie[] cookies = request.getCookies();
		for(Cookie cookie:cookies) {
			
			if(cookie.getName().equals("at")) at=cookie.getValue();
			if(cookie.getName().equals("rt")) rt=cookie.getValue();
		}
		String username=null;
		if(at==null || rt==null) throw new UserNotLoggedInException("User Not Logged in");
		Optional<AccessToken> accessToken = accessTokenRepoistory.findByTokenAndIsblocked(at,false);
		if(accessToken==null) throw new RuntimeException();
		else
		{    log.error("Authenticating the token.......");
			 username = jwtService.extractUsername(at);
			if(username==null) throw new InvalidAuthenticationException("Failed to Authenticate");
			UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null,userDetails.getAuthorities());
			token.setDetails(new WebAuthenticationDetails(request));
			SecurityContextHolder.getContext().setAuthentication(token);
			log.info("Authenticated SUcesfully");
		}
		filterChain.doFilter(request, response);
		
	}
	

}
