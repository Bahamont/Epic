package com.epic.quotation;

import java.time.LocalDateTime;

import com.epic.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "quotation_requests")
public class QuotationRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre del cliente es obligatorio")
	@Size(max = 120)
	@Column(nullable = false, length = 120)
	private String clientName;

	@NotBlank(message = "El correo del cliente es obligatorio")
	@Email(message = "El correo del cliente no es válido")
	@Size(max = 160)
	@Column(nullable = false, length = 160)
	private String clientEmail;

	@Size(max = 30)
	@Column(length = 30)
	private String clientPhone;

	@NotNull(message = "El producto es obligatorio")
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Product product;

	@NotNull(message = "La cantidad es obligatoria")
	@Min(value = 1, message = "La cantidad mínima es 1")
	@Column(nullable = false)
	private Integer quantity;

	@Size(max = 1000)
	@Column(length = 1000)
	private String message;

	@Column(nullable = false)
	private LocalDateTime requestedAt;

	@PrePersist
	void onCreate() {
		if (requestedAt == null) {
			requestedAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public String getClientPhone() {
		return clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}
}
