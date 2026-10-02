package com.epic.quotation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuotationRequestForm {

	@NotBlank(message = "El nombre del cliente es obligatorio")
	@Size(max = 120)
	private String clientName;

	@NotBlank(message = "El correo del cliente es obligatorio")
	@Email(message = "El correo del cliente no es válido")
	@Size(max = 160)
	private String clientEmail;

	@Size(max = 30)
	private String clientPhone;

	@NotNull(message = "El id del producto es obligatorio")
	private Long productId;

	@NotNull(message = "La cantidad es obligatoria")
	@Min(value = 1, message = "La cantidad mínima es 1")
	private Integer quantity;

	@Size(max = 1000)
	private String message;

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

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
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
}
