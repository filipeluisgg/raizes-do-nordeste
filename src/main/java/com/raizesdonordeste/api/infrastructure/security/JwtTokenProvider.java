package com.raizesdonordeste.api.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.raizesdonordeste.api.domain.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long expirationMs;

	public JwtTokenProvider(
			@Value("${api.security.jwt.secret}") String secret,
			@Value("${api.security.jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String gerarToken(Usuario usuario) {
		Date agora = new Date();
		Date expiracao = new Date(agora.getTime() + expirationMs);

		String features = usuario.getRole().getFeatures().stream()
			.sorted()
			.collect(Collectors.joining(","));

		return Jwts.builder()
			.subject(usuario.getId().toString())
			.claim("email", usuario.getEmail())
			.claim("role", usuario.getRole().name())
			.claim("features", features)
			.issuedAt(agora)
			.expiration(expiracao)
			.signWith(key)
			.compact();
	}

	public Claims extrairClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean validarToken(String token) {
		try {
			extrairClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Long extrairUsuarioId(String token) {
		return Long.valueOf(extrairClaims(token).getSubject());
	}

	public long getExpirationMs() {
		return expirationMs;
	}
}
