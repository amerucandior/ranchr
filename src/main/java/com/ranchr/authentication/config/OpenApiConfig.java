package com.ranchr.authentication.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME_NAME = "bearerAuth";

	@Bean
	public OpenAPI customOpenApi() {
		return new OpenAPI()
					   .info(new Info()
									 .title("Ranchr")
									 .description("A trusted marketplace -- accessible by anyone with a basic phone.")
									 .version("1.0.0"))
					   .components(new Components()
										   .addSecuritySchemes(SECURITY_SCHEME_NAME,
												   new SecurityScheme()
														   .name(SECURITY_SCHEME_NAME)
														   .type(SecurityScheme.Type.HTTP)
														   .scheme("bearer")
														   .bearerFormat("JWT")
														   .description("JWT access token obtained from POST /auth/login or /auth/register")
										   )
					   );
	}
}
