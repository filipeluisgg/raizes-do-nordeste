package com.raizesdonordeste.api.config;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.raizesdonordeste.api.domain.exception.NegocioException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<Map<String, Object>> handleNegocioException(NegocioException ex) {
		Map<String, Object> body = Map.of(
			"name", ex.getName(),
			"message", ex.getMessage(),
			"action", ex.getAction(),
			"status_code", ex.getStatusCode()
		);
		return ResponseEntity.status(ex.getStatusCode()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
		String mensagem = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.reduce((a, b) -> a + "; " + b)
			.orElse("Erro de validação nos dados enviados.");

		Map<String, Object> body = Map.of(
			"name", "ValidationError",
			"message", mensagem,
			"action", "Corrija os campos indicados e tente novamente.",
			"status_code", 400
		);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		Map<String, Object> body = Map.of(
			"name", "BadRequestError",
			"message", ex.getMessage(),
			"action", "Verifique os dados enviados na requisição.",
			"status_code", 400
		);
		return ResponseEntity.badRequest().body(body);
	}
}
