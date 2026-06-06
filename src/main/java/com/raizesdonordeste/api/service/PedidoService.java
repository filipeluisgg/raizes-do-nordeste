package com.raizesdonordeste.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.ItemPedido;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@Service
public class PedidoService {

	private final PedidoRepository pedidoRepository;
	private final UsuarioRepository usuarioRepository;
	private final UnidadeRepository unidadeRepository;
	private final ProdutoRepository produtoRepository;
	private final EstoqueService estoqueService;

	public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
			UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
			EstoqueService estoqueService) {
		this.pedidoRepository = pedidoRepository;
		this.usuarioRepository = usuarioRepository;
		this.unidadeRepository = unidadeRepository;
		this.produtoRepository = produtoRepository;
		this.estoqueService = estoqueService;
	}

	@Transactional
	public Pedido criarPedido(Long clienteId, Long unidadeId, CanalPedido canal, String observacao,
			List<ItemRequest> itensRequest) {

		Usuario cliente = usuarioRepository.findById(clienteId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", clienteId));

		Unidade unidade = unidadeRepository.findById(unidadeId)
			.filter(Unidade::getAtiva)
			.orElseThrow(() -> new NegocioException(
				"UnidadeInativaError",
				"A unidade com ID " + unidadeId + " não está ativa ou não foi encontrada.",
				"Selecione uma unidade ativa para realizar o pedido.",
				422
			));

		if (itensRequest == null || itensRequest.isEmpty()) {
			throw new NegocioException(
				"ValidationError",
				"O pedido deve conter pelo menos um item.",
				"Adicione itens ao pedido antes de enviá-lo.",
				400
			);
		}

		Pedido pedido = new Pedido(cliente, unidade, canal);
		pedido.setObservacao(observacao);

		for (ItemRequest item : itensRequest) {
			Produto produto = produtoRepository.findById(item.produtoId())
				.filter(Produto::getAtivo)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", item.produtoId()));

			estoqueService.validarEBaixarEstoque(unidadeId, produto.getId(), item.quantidade());

			ItemPedido itemPedido = new ItemPedido(produto, item.quantidade(), produto.getPreco());
			pedido.adicionarItem(itemPedido);
		}

		return pedidoRepository.save(pedido);
	}

	@Transactional
	public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
		Pedido pedido = pedidoRepository.findById(pedidoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));

		StatusPedido statusAnterior = pedido.getStatus();
		pedido.avancarStatus(novoStatus);

		if (novoStatus == StatusPedido.CANCELADO) {
			pedido.getItens().forEach(item ->
				estoqueService.estornarEstoque(
					pedido.getUnidade().getId(),
					item.getProduto().getId(),
					item.getQuantidade()
				)
			);
		}

		return pedidoRepository.save(pedido);
	}

	public Pedido buscarPorId(Long pedidoId) {
		return pedidoRepository.findById(pedidoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));
	}

	public List<Pedido> listarPorFiltros(CanalPedido canal, StatusPedido status, Long unidadeId) {
		if (canal != null && unidadeId != null) {
			return pedidoRepository.findByUnidadeIdAndCanalPedido(unidadeId, canal);
		}
		if (canal != null) {
			return pedidoRepository.findByCanalPedido(canal);
		}
		if (status != null) {
			return pedidoRepository.findByStatus(status);
		}
		if (unidadeId != null) {
			return pedidoRepository.findByUnidadeId(unidadeId);
		}
		return pedidoRepository.findAll();
	}

	public List<Pedido> listarPorCliente(Long clienteId) {
		return pedidoRepository.findByClienteId(clienteId);
	}

	public record ItemRequest(Long produtoId, int quantidade) {}
}
