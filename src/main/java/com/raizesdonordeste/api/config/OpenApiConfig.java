package com.raizesdonordeste.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Raízes do Nordeste API")
				.version("0.1.0")
				.description("API REST para gestão de pedidos multi-canal, estoque por unidade, programa de fidelidade e integração de pagamento da rede Raízes do Nordeste.")
				.contact(new Contact()
					.name("Luis Felipe Guerreiro Ota")
					.email("filipeluisgg+dev@gmail.com")))
			.addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
			.components(new Components()
				.addSecuritySchemes("Bearer JWT", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("Insira o token JWT obtido via POST /auth/login")));
	}
}
