package com.raizesdonordeste.api.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "produto")
public class Produto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(length = 500)
	private String descricao;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal preco;

	@Column(nullable = false, length = 100)
	private String categoria;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(nullable = false)
	private Boolean sazonal = false;

	@Column(name = "disponivel_de")
	private LocalDate disponivelDe;

	@Column(name = "disponivel_ate")
	private LocalDate disponivelAte;

	@CreationTimestamp
	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected Produto() {
	}

	public Produto(String nome, String descricao, BigDecimal preco, String categoria) {
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.categoria = categoria;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getSazonal() {
		return sazonal;
	}

	public void setSazonal(Boolean sazonal) {
		this.sazonal = sazonal;
	}

	public LocalDate getDisponivelDe() {
		return disponivelDe;
	}

	public void setDisponivelDe(LocalDate disponivelDe) {
		this.disponivelDe = disponivelDe;
	}

	public LocalDate getDisponivelAte() {
		return disponivelAte;
	}

	public void setDisponivelAte(LocalDate disponivelAte) {
		this.disponivelAte = disponivelAte;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public boolean isDisponivelNoPeriodo(LocalDate data) {
		if (!Boolean.TRUE.equals(sazonal)) {
			return true;
		}
		if (disponivelDe != null && data.isBefore(disponivelDe)) {
			return false;
		}
		if (disponivelAte != null && data.isAfter(disponivelAte)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Produto produto = (Produto) o;
		return id != null && Objects.equals(id, produto.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
