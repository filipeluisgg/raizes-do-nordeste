package com.raizesdonordeste.api.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.UpdateTimestamp;

import com.raizesdonordeste.api.domain.exception.EstoqueInsuficienteException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "estoque_unidade",
	uniqueConstraints = @UniqueConstraint(columnNames = {"unidade_id", "produto_id"})
)
public class EstoqueUnidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unidade_id", nullable = false)
	private Unidade unidade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(nullable = false)
	private Integer quantidade = 0;

	@Column(name = "estoque_minimo", nullable = false)
	private Integer estoqueMinimo = 0;

	@UpdateTimestamp
	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	protected EstoqueUnidade() {
	}

	public EstoqueUnidade(Unidade unidade, Produto produto, Integer quantidade, Integer estoqueMinimo) {
		this.unidade = unidade;
		this.produto = produto;
		this.quantidade = quantidade;
		this.estoqueMinimo = estoqueMinimo;
	}

	public void baixar(int qtd) {
		if (qtd > this.quantidade) {
			throw new EstoqueInsuficienteException(produto.getNome(), this.quantidade, qtd);
		}
		this.quantidade -= qtd;
	}

	public void repor(int qtd) {
		this.quantidade += qtd;
	}

	public boolean abaixoDoMinimo() {
		return this.quantidade < this.estoqueMinimo;
	}

	public Long getId() {
		return id;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public Integer getEstoqueMinimo() {
		return estoqueMinimo;
	}

	public void setEstoqueMinimo(Integer estoqueMinimo) {
		this.estoqueMinimo = estoqueMinimo;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EstoqueUnidade that = (EstoqueUnidade) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
