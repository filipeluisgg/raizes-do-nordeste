package com.raizesdonordeste.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.PontoFidelidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PontoFidelidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@Service
public class FidelidadeService {

	private final PontoFidelidadeRepository pontoRepository;
	private final FidelidadeConsentimentoRepository consentimentoRepository;
	private final UsuarioRepository usuarioRepository;

	public FidelidadeService(PontoFidelidadeRepository pontoRepository,
			FidelidadeConsentimentoRepository consentimentoRepository,
			UsuarioRepository usuarioRepository) {
		this.pontoRepository = pontoRepository;
		this.consentimentoRepository = consentimentoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public void creditarPontos(Pedido pedido) {
		Long clienteId = pedido.getCliente().getId();

		if (!possuiConsentimentoAtivo(clienteId)) {
			return;
		}

		if (pontoRepository.existsByPedidoId(pedido.getId())) {
			return;
		}

		int pontos = pedido.getValorTotal().intValue();
		if (pontos <= 0) {
			return;
		}

		PontoFidelidade ponto = new PontoFidelidade(pedido.getCliente(), pedido, pontos);
		pontoRepository.save(ponto);
	}

	public boolean possuiConsentimentoAtivo(Long clienteId) {
		FidelidadeConsentimento ultimo = consentimentoRepository.findConsentimentoAtual(clienteId);
		return ultimo != null && ultimo.getConsentimento();
	}

	@Transactional
	public FidelidadeConsentimento atualizarConsentimento(Long clienteId, boolean consentimento,
			String versaoTermo, String ipOrigem) {
		Usuario cliente = usuarioRepository.findById(clienteId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", clienteId));

		FidelidadeConsentimento novoConsentimento = new FidelidadeConsentimento(
			cliente, consentimento, ipOrigem, versaoTermo
		);
		return consentimentoRepository.save(novoConsentimento);
	}

	public int calcularSaldo(Long clienteId) {
		return pontoRepository.calcularSaldoPontos(clienteId);
	}

	public List<PontoFidelidade> listarHistorico(Long clienteId) {
		return pontoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId);
	}
}
