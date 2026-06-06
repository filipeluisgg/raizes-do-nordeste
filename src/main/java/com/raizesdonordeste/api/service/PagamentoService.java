package com.raizesdonordeste.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.Pagamento;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;
import com.raizesdonordeste.api.domain.enums.StatusPagamento;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway.PagamentoGatewayResponse;
import com.raizesdonordeste.api.infrastructure.repository.PagamentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;

@Service
public class PagamentoService {

	private final PagamentoRepository pagamentoRepository;
	private final PedidoRepository pedidoRepository;
	private final PagamentoGateway pagamentoGateway;
	private final FidelidadeService fidelidadeService;

	public PagamentoService(PagamentoRepository pagamentoRepository,
			PedidoRepository pedidoRepository,
			PagamentoGateway pagamentoGateway,
			FidelidadeService fidelidadeService) {
		this.pagamentoRepository = pagamentoRepository;
		this.pedidoRepository = pedidoRepository;
		this.pagamentoGateway = pagamentoGateway;
		this.fidelidadeService = fidelidadeService;
	}

	@Transactional
	public Pagamento processarPagamento(Long pedidoId, FormaPagamento formaPagamento) {
		Pedido pedido = pedidoRepository.findById(pedidoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));

		if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
			throw new NegocioException(
				"PagamentoInvalidoError",
				"O pedido não está no status adequado para pagamento.",
				"Apenas pedidos com status 'AGUARDANDO_PAGAMENTO' podem ser pagos.",
				409
			);
		}

		if (pagamentoRepository.existsByPedidoId(pedidoId)) {
			throw new NegocioException(
				"PagamentoDuplicadoError",
				"Já existe um pagamento registrado para este pedido.",
				"Consulte o status do pagamento existente.",
				409
			);
		}

		PagamentoGatewayResponse gatewayResponse;
		try {
			gatewayResponse = pagamentoGateway.processar(pedido, formaPagamento);
		} catch (Exception e) {
			Pagamento pagamentoFalha = new Pagamento(pedido, formaPagamento, pedido.getValorTotal());
			pagamentoFalha.setStatus(StatusPagamento.RECUSADO);
			pagamentoFalha.setMotivoRecusa("Falha na comunicação com o gateway: " + e.getMessage());
			return pagamentoRepository.save(pagamentoFalha);
		}

		if (gatewayResponse.aprovado()) {
			Pagamento pagamento = new Pagamento(pedido, formaPagamento, pedido.getValorTotal());
			pagamento.setStatus(StatusPagamento.APROVADO);
			pagamento.setTransacaoExternaId(gatewayResponse.transacaoId());
			pagamentoRepository.save(pagamento);

			pedido.avancarStatus(StatusPedido.RECEBIDO);
			pedidoRepository.save(pedido);

			fidelidadeService.creditarPontos(pedido);

			return pagamento;
		}

		Pagamento pagamento = new Pagamento(pedido, formaPagamento, pedido.getValorTotal());
		pagamento.setStatus(StatusPagamento.RECUSADO);
		pagamento.setMotivoRecusa(gatewayResponse.motivoRecusa());
		return pagamentoRepository.save(pagamento);
	}
}
