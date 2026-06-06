package com.raizesdonordeste.api.domain.entity;

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
@Table(name = "fidelidade_consentimento")
public class FidelidadeConsentimento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Usuario cliente;

	@Column(nullable = false)
	private Boolean consentimento;

	@Column(name = "ip_origem", length = 45)
	private String ipOrigem;

	@Column(name = "versao_termo", nullable = false, length = 20)
	private String versaoTermo;

	@CreationTimestamp
	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected FidelidadeConsentimento() {
	}

	public FidelidadeConsentimento(Usuario cliente, Boolean consentimento, String ipOrigem, String versaoTermo) {
		this.cliente = cliente;
		this.consentimento = consentimento;
		this.ipOrigem = ipOrigem;
		this.versaoTermo = versaoTermo;
	}

	public Long getId() {
		return id;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public Boolean getConsentimento() {
		return consentimento;
	}

	public void setConsentimento(Boolean consentimento) {
		this.consentimento = consentimento;
	}

	public String getIpOrigem() {
		return ipOrigem;
	}

	public void setIpOrigem(String ipOrigem) {
		this.ipOrigem = ipOrigem;
	}

	public String getVersaoTermo() {
		return versaoTermo;
	}

	public void setVersaoTermo(String versaoTermo) {
		this.versaoTermo = versaoTermo;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FidelidadeConsentimento that = (FidelidadeConsentimento) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
