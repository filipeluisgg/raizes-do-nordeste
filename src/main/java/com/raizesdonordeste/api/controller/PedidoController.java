package com.raizesdonordeste.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.dto.request.AtualizarStatusRequest;
import com.raizesdonordeste.api.dto.request.CriarPedidoRequest;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.service.AuditoriaService;
import com.raizesdonordeste.api.service.PedidoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;
	private final AuditoriaService auditoriaService;

	public PedidoController(PedidoService pedidoService, AuditoriaService auditoriaService) {
		this.pedidoService = pedidoService;
		this.auditoriaService = auditoriaService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('create:pedido')")
	public ResponseEntity<Map<String, Object>> criarPedido(
			@Valid @RequestBody CriarPedidoRequest request,
			Authentication auth) {

		Long clienteId = (Long) auth.getPrincipal();

		List<PedidoService.ItemRequest> itens = request.itens().stream()
			.map(i -> new PedidoService.ItemRequest(i.produtoId(), i.quantidade()))
			.toList();

		Pedido pedido = pedidoService.criarPedido(
			clienteId, request.unidadeId(), request.canalPedido(),
			request.observacao(), itens
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(mapPedidoResponse(pedido));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('read:pedido')")
	public ResponseEntity<List<Map<String, Object>>> listarPedidos(
			@RequestParam(required = false) CanalPedido canalPedido,
			@RequestParam(required = false) StatusPedido status,
			@RequestParam(required = false) Long unidadeId) {

		List<Pedido> pedidos = pedidoService.listarPorFiltros(canalPedido, status, unidadeId);

		List<Map<String, Object>> response = pedidos.stream()
			.map(this::mapPedidoResponse)
			.toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('read:pedido')")
	public ResponseEntity<Map<String, Object>> buscarPedido(@PathVariable Long id) {
		Pedido pedido = pedidoService.buscarPorId(id);
		return ResponseEntity.ok(mapPedidoResponse(pedido));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAuthority('update:pedido:status') or hasAuthority('cancel:pedido')")
	public ResponseEntity<Map<String, Object>> atualizarStatus(
			@PathVariable Long id,
			@Valid @RequestBody AtualizarStatusRequest request,
			Authentication auth,
			HttpServletRequest httpRequest) {

		if (request.novoStatus() == StatusPedido.CANCELADO) {
			boolean podeCancelar = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("cancel:pedido"));
			if (!podeCancelar) {
				throw new NegocioException(
					"ForbiddenError",
					"Usuário não tem permissão para cancelar pedidos.",
					"Apenas usuários com o perfil de GERENTE podem executar cancelamentos.",
					403
				);
			}
		}

		Pedido pedido = pedidoService.buscarPorId(id);
		StatusPedido anterior = pedido.getStatus();

		pedido = pedidoService.atualizarStatus(id, request.novoStatus());

		if (request.novoStatus() == StatusPedido.CANCELADO) {
			auditoriaService.registrar(
				(Long) auth.getPrincipal(), "CANCELAMENTO", "Pedido", id,
				"{\"status\": \"" + anterior + "\"}",
				"{\"status\": \"CANCELADO\"}",
				request.motivo(),
				httpRequest.getRemoteAddr()
			);
		}

		Map<String, Object> response = Map.of(
			"pedidoId", pedido.getId(),
			"anterior", anterior.name(),
			"novo", pedido.getStatus().name()
		);
		return ResponseEntity.ok(response);
	}

	private Map<String, Object> mapPedidoResponse(Pedido pedido) {
		List<Map<String, Object>> itens = pedido.getItens().stream()
			.map(item -> Map.<String, Object>of(
				"produtoId", item.getProduto().getId(),
				"produtoNome", item.getProduto().getNome(),
				"quantidade", item.getQuantidade(),
				"precoUnitario", item.getPrecoUnitario(),
				"subtotal", item.getSubtotal()
			))
			.toList();

		return Map.of(
			"pedidoId", pedido.getId(),
			"canalPedido", pedido.getCanalPedido().name(),
			"status", pedido.getStatus().name(),
			"valorTotal", pedido.getValorTotal(),
			"itens", itens,
			"criadoEm", pedido.getCriadoEm().toString()
		);
	}
}
