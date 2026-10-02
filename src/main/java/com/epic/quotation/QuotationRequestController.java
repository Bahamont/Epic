package com.epic.quotation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quotations")
public class QuotationRequestController {

	private final QuotationRequestService quotationRequestService;

	public QuotationRequestController(QuotationRequestService quotationRequestService) {
		this.quotationRequestService = quotationRequestService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public QuotationRequest register(@Valid @RequestBody QuotationRequestForm form) {
		return quotationRequestService.register(form);
	}

	@GetMapping
	public List<QuotationRequest> listAll() {
		return quotationRequestService.findAll();
	}

	@GetMapping("/product/{productId}")
	public List<QuotationRequest> listByProduct(@PathVariable Long productId) {
		return quotationRequestService.findByProductId(productId);
	}
}
