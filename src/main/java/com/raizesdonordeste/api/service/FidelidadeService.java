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
	private final com.raizesdonordeste.api.infrastructure.repository.PedidoRepository pedidoRepository;

	public FidelidadeService(PontoFidelidadeRepository pontoRepository,
			FidelidadeConsentimentoRepository consentimentoRepository,
			UsuarioRepository usuarioRepository,
			com.raizesdonordeste.api.infrastructure.repository.PedidoRepository pedidoRepository) {
		this.pontoRepository = pontoRepository;
		this.consentimentoRepository = consentimentoRepository;
		this.usuarioRepository = usuarioRepository;
		this.pedidoRepository = pedidoRepository;
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

	@Transactional
	public void resgatarPontos(Long clienteId, Long pedidoId, int pontosAResgatar) {
		if (pontosAResgatar <= 0) {
			throw new com.raizesdonordeste.api.domain.exception.NegocioException("PontosInvalidos", "A quantidade de pontos deve ser maior que zero.", "Informe um valor positivo.", 400);
		}
		int saldo = calcularSaldo(clienteId);
		if (saldo < pontosAResgatar) {
			throw new com.raizesdonordeste.api.domain.exception.NegocioException("SaldoInsuficiente", "Saldo de pontos insuficiente.", "Diminua os pontos ou ganhe mais.", 400);
		}
		
		Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, clienteId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));
			
		// Validar se já não foi resgatado (regra simples: pedido só aceita 1 resgate)
		if (pontoRepository.existsByPedidoIdAndPontosLessThan(pedidoId, 0)) {
		    throw new com.raizesdonordeste.api.domain.exception.NegocioException("ResgateDuplicado", "Já houve resgate de pontos para este pedido.", "Utilize outro pedido.", 400);
		}

		java.math.BigDecimal desconto = java.math.BigDecimal.valueOf(pontosAResgatar); // 1 ponto = 1 real
		java.math.BigDecimal novoValor = pedido.getValorTotal().subtract(desconto);
		if (novoValor.compareTo(java.math.BigDecimal.ZERO) < 0) {
		    novoValor = java.math.BigDecimal.ZERO;
		}
		pedido.setValorTotal(novoValor);
		pedidoRepository.save(pedido);

		Usuario cliente = usuarioRepository.findById(clienteId).orElseThrow();
		PontoFidelidade debito = new PontoFidelidade(cliente, pedido, -pontosAResgatar);
		pontoRepository.save(debito);
	}
}
