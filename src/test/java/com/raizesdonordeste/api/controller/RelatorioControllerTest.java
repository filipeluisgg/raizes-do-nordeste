package com.raizesdonordeste.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.raizesdonordeste.api.dto.RelatorioVendasDTO;
import com.raizesdonordeste.api.service.RelatorioService;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

	@Mock
	private RelatorioService relatorioService;

	@InjectMocks
	private RelatorioController relatorioController;

	@Test
	@DisplayName("Deve retornar relatorio com sucesso")
	void deveRetornarRelatorio() {
		// Arrange
		RelatorioVendasDTO dto = new RelatorioVendasDTO(1L, "Unidade Centro", 50L, new BigDecimal("2500.00"));
		when(relatorioService.gerarRelatorioPorUnidade(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
			.thenReturn(dto);

		// Act
		ResponseEntity<RelatorioVendasDTO> response = relatorioController.gerarRelatorioVendas(1L, 30);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1L, response.getBody().getUnidadeId());
		assertEquals("Unidade Centro", response.getBody().getNomeUnidade());
	}
}
