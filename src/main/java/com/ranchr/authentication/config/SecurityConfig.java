package com.ranchr.authentication.config;

import com.ranchr.authentication.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService userDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsConfigurationSource corsConfigurationSource;

	private static final String[] PUBLIC_ENDPOINTS = {
			"/auth/register",
			"/auth/login",
			"/auth/refresh",
			"/actuator/health",
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/v3/api-docs/**"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
					   // ✅ Disable CSRF — stateless JWT API does not need it
					   .csrf(AbstractHttpConfigurer::disable)
					   // Configure CORS via CorsConfig bean
					   .cors(cors -> cors.configurationSource(corsConfigurationSource))
					   .sessionManagement(session ->
												  session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

					   // authorization rules
					   .authorizeHttpRequests(auth -> auth
															  .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
															  .requestMatchers("/admin/**").hasRole("VET")
															  .requestMatchers(HttpMethod.GET, "/api/listings", "/api/listings/{id}").permitAll()
															  .anyRequest().authenticated()
					   )
					   .authenticationProvider(authenticationProvider())
					   .addFilterBefore(
							   jwtAuthenticationFilter,
							   UsernamePasswordAuthenticationFilter.class
					   )
					   .build();

	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration config
	) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
}
