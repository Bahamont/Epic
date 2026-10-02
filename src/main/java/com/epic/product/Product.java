package com.epic.product;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120)
	@Column(nullable = false, length = 120)
	private String name;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 1000)
	@Column(nullable = false, length = 1000)
	private String description;

	@NotBlank(message = "La categoría es obligatoria")
	@Size(max = 80)
	@Column(nullable = false, length = 80)
	private String category;

	@NotNull(message = "El precio base es obligatorio")
	@DecimalMin(value = "0.0", inclusive = true, message = "El precio base no puede ser negativo")
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal basePrice;

	public Product() {
	}

	public Product(String name, String description, String category, BigDecimal basePrice) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.basePrice = basePrice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}
}
