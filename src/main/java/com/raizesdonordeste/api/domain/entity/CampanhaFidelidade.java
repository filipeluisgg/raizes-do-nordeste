package com.raizesdonordeste.api.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "campanha_fidelidade")
public class CampanhaFidelidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(nullable = false, precision = 4, scale = 2)
	private BigDecimal multiplicador = BigDecimal.ONE;

	@Column(nullable = false)
	private LocalDateTime inicio;

	@Column(nullable = false)
	private LocalDateTime fim;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unidade_id")
	private Unidade unidade;

	@Column(nullable = false)
	private Boolean ativa = true;

	@CreationTimestamp
	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected CampanhaFidelidade() {
	}

	public CampanhaFidelidade(String nome, BigDecimal multiplicador, LocalDateTime inicio, LocalDateTime fim, Unidade unidade) {
		this.nome = nome;
		this.multiplicador = multiplicador;
		this.inicio = inicio;
		this.fim = fim;
		this.unidade = unidade;
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

	public BigDecimal getMultiplicador() {
		return multiplicador;
	}

	public void setMultiplicador(BigDecimal multiplicador) {
		this.multiplicador = multiplicador;
	}

	public LocalDateTime getInicio() {
		return inicio;
	}

	public void setInicio(LocalDateTime inicio) {
		this.inicio = inicio;
	}

	public LocalDateTime getFim() {
		return fim;
	}

	public void setFim(LocalDateTime fim) {
		this.fim = fim;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public boolean isValidaNoPeriodo(LocalDateTime dataHora) {
		return ativa && !dataHora.isBefore(inicio) && !dataHora.isAfter(fim);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CampanhaFidelidade that = (CampanhaFidelidade) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
