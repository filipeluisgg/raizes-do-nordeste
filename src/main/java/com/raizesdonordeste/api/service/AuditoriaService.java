package com.raizesdonordeste.api.service;

import org.springframework.stereotype.Service;

import com.raizesdonordeste.api.domain.entity.AuditoriaLog;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.AuditoriaLogRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@Service
public class AuditoriaService {

	private final AuditoriaLogRepository auditoriaRepository;
	private final UsuarioRepository usuarioRepository;

	public AuditoriaService(AuditoriaLogRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
		this.auditoriaRepository = auditoriaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public void registrar(Long usuarioId, String acao, String entidade, Long entidadeId,
			String dadosAntes, String dadosDepois, String motivo, String ipOrigem) {

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));

		AuditoriaLog log = new AuditoriaLog(usuario, acao, entidade, entidadeId);
		log.setDadosAntes(dadosAntes);
		log.setDadosDepois(dadosDepois);
		log.setMotivo(motivo);
		log.setIpOrigem(ipOrigem);

		auditoriaRepository.save(log);
	}

	public java.util.List<AuditoriaLog> listar() {
		return auditoriaRepository.findAllByOrderByCriadoEmDesc();
	}

	public java.util.List<AuditoriaLog> listarPorEntidade(String entidade, Long entidadeId) {
		return auditoriaRepository.findByEntidadeAndEntidadeId(entidade, entidadeId);
	}
}
