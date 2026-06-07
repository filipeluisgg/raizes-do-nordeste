package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.raizesdonordeste.api.dto.RelatorioVendasDTO;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.enums.StatusPedido;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@InjectMocks
	private RelatorioService relatorioService;

	@Test
	@DisplayName("Deve gerar relatório de vendas consolidado por unidade")
	void deveGerarRelatorioVendas() {
		// Arrange
		Long unidadeId = 1L;
		LocalDateTime inicio = LocalDateTime.now().minusDays(30);
		LocalDateTime fim = LocalDateTime.now();
		
		Unidade unidade = new Unidade("Unidade Centro", "Rua X", "Natal", "RN", true);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		Pedido p1 = new Pedido(null, unidade, null);
		ReflectionTestUtils.setField(p1, "id", 10L);
		ReflectionTestUtils.setField(p1, "status", StatusPedido.ENTREGUE);
		p1.setValorTotal(new BigDecimal("150.00"));
		
		Pedido p2 = new Pedido(null, unidade, null);
		ReflectionTestUtils.setField(p2, "id", 11L);
		ReflectionTestUtils.setField(p2, "status", StatusPedido.ENTREGUE);
		p2.setValorTotal(new BigDecimal("50.00"));

		when(pedidoRepository.findByUnidadeIdAndStatusAndCriadoEmBetween(
				unidadeId, StatusPedido.ENTREGUE, inicio, fim))
				.thenReturn(List.of(p1, p2));

		// Act
		RelatorioVendasDTO dto = relatorioService.gerarRelatorioPorUnidade(unidadeId, inicio, fim);

		// Assert
		assertEquals(unidadeId, dto.getUnidadeId());
		assertEquals("Unidade Centro", dto.getNomeUnidade());
		assertEquals(2L, dto.getTotalPedidos());
		assertEquals(new BigDecimal("200.00"), dto.getValorTotal());
	}
}
