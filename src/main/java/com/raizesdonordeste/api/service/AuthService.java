package com.raizesdonordeste.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;
import com.raizesdonordeste.api.infrastructure.security.JwtTokenProvider;

@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final FidelidadeConsentimentoRepository consentimentoRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(UsuarioRepository usuarioRepository,
			FidelidadeConsentimentoRepository consentimentoRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider) {
		this.usuarioRepository = usuarioRepository;
		this.consentimentoRepository = consentimentoRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional
	public Usuario registrar(String nome, String email, String senha, String telefone,
			boolean consentimentoFidelidade, String ipOrigem) {

		if (usuarioRepository.existsByEmail(email)) {
			throw new NegocioException(
				"ConflictError",
				"Já existe um usuário cadastrado com o e-mail '" + email + "'.",
				"Utilize outro endereço de e-mail ou faça login com o existente.",
				409
			);
		}

		String senhaHash = passwordEncoder.encode(senha);
		Usuario usuario = new Usuario(nome, email, senhaHash, Role.CLIENTE);
		usuario.setTelefone(telefone);
		usuario = usuarioRepository.save(usuario);

		FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
			usuario, consentimentoFidelidade, ipOrigem, "1.0"
		);
		consentimentoRepository.save(consentimento);

		return usuario;
	}

	public String login(String email, String senha) {
		Usuario usuario = usuarioRepository.findByEmail(email)
			.filter(u -> u.getAtivo())
			.orElseThrow(() -> new NegocioException(
				"UnauthorizedError",
				"Credenciais inválidas.",
				"Verifique o e-mail e a senha informados.",
				401
			));

		if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
			throw new NegocioException(
				"UnauthorizedError",
				"Credenciais inválidas.",
				"Verifique o e-mail e a senha informados.",
				401
			);
		}

		return jwtTokenProvider.gerarToken(usuario);
	}

	public long getTokenExpirationMs() {
		return jwtTokenProvider.getExpirationMs();
	}
}
