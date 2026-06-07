package com.raizesdonordeste.api.dto;

import java.math.BigDecimal;

public class RelatorioVendasDTO {
	private Long unidadeId;
	private String nomeUnidade;
	private Long totalPedidos;
	private BigDecimal valorTotal;

	public RelatorioVendasDTO() {
	}

	public RelatorioVendasDTO(Long unidadeId, String nomeUnidade, Long totalPedidos, BigDecimal valorTotal) {
		this.unidadeId = unidadeId;
		this.nomeUnidade = nomeUnidade;
		this.totalPedidos = totalPedidos;
		this.valorTotal = valorTotal;
	}

	public Long getUnidadeId() {
		return unidadeId;
	}

	public void setUnidadeId(Long unidadeId) {
		this.unidadeId = unidadeId;
	}

	public String getNomeUnidade() {
		return nomeUnidade;
	}

	public void setNomeUnidade(String nomeUnidade) {
		this.nomeUnidade = nomeUnidade;
	}

	public Long getTotalPedidos() {
		return totalPedidos;
	}

	public void setTotalPedidos(Long totalPedidos) {
		this.totalPedidos = totalPedidos;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}
}
