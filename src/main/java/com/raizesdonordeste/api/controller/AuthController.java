package com.raizesdonordeste.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.dto.request.LoginRequest;
import com.raizesdonordeste.api.dto.request.RegistroUsuarioRequest;
import com.raizesdonordeste.api.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/registro")
	public ResponseEntity<Map<String, Object>> registrar(
			@Valid @RequestBody RegistroUsuarioRequest request,
			HttpServletRequest httpRequest) {

		String ip = httpRequest.getRemoteAddr();
		Usuario usuario = authService.registrar(
			request.nome(), request.email(), request.senha(),
			request.telefone(), request.consentimentoFidelidade(), ip
		);

		Map<String, Object> response = Map.of(
			"id", usuario.getId(),
			"nome", usuario.getNome(),
			"email", usuario.getEmail(),
			"role", usuario.getRole().name()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
		String token = authService.login(request.email(), request.senha());

		Map<String, Object> response = Map.of(
			"token", token,
			"tipo", "Bearer",
			"expiraEm", authService.getTokenExpirationMs()
		);
		return ResponseEntity.ok(response);
	}
}
