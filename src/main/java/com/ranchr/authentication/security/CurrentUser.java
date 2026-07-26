package com.ranchr.authentication.security;


import com.ranchr.authentication.model.User;
import com.ranchr.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUser {

	private final UserRepository userRepository;

	/**
	 * Loads the User entity for the currently authenticated principal.
	 * Throws if there's no authenticated context or the user no longer exists.
	 */
	public User get() {
		String username = getUsername();
		return userRepository.findByUsername(username)
					   .orElseThrow(() -> new UsernameNotFoundException(
							   "No user found for username: " + username));
	}

	/** Cheaper path when you only need the username (e.g. logging, simple queries). */
	public String getUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return Objects.requireNonNull(auth, "No authenticated user in context").getName();
	}

	public UUID getUserId() {
		return userRepository.findUserIdByUsername(getUsername())
					   .orElseThrow(()-> new UsernameNotFoundException(
							   "No user found for username"
					   ));
	}

	public boolean isAuthenticated() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated()
					   && !(auth instanceof AnonymousAuthenticationToken);
	}
}
