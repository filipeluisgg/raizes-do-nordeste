package com.raizesdonordeste.api.infrastructure.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = extrairToken(request);

		if (token != null && jwtTokenProvider.validarToken(token)) {
			Claims claims = jwtTokenProvider.extrairClaims(token);

			String features = claims.get("features", String.class);
			List<SimpleGrantedAuthority> authorities = Arrays.stream(features.split(","))
				.filter(f -> !f.isBlank())
				.map(SimpleGrantedAuthority::new)
				.toList();

			Long usuarioId = Long.valueOf(claims.getSubject());
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(usuarioId, null, authorities);

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String extrairToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}
}
