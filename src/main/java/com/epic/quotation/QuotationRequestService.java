package com.epic.quotation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.epic.product.Product;
import com.epic.product.ProductService;

@Service
public class QuotationRequestService {

	private final QuotationRequestRepository quotationRequestRepository;
	private final ProductService productService;

	public QuotationRequestService(
			QuotationRequestRepository quotationRequestRepository,
			ProductService productService) {
		this.quotationRequestRepository = quotationRequestRepository;
		this.productService = productService;
	}

	public QuotationRequest register(QuotationRequestForm form) {
		Product product = productService.findById(form.getProductId());

		QuotationRequest request = new QuotationRequest();
		request.setClientName(form.getClientName());
		request.setClientEmail(form.getClientEmail());
		request.setClientPhone(form.getClientPhone());
		request.setProduct(product);
		request.setQuantity(form.getQuantity());
		request.setMessage(form.getMessage());

		return quotationRequestRepository.save(request);
	}

	public List<QuotationRequest> findAll() {
		return quotationRequestRepository.findAll();
	}

	public List<QuotationRequest> findByProductId(Long productId) {
		productService.findById(productId);
		return quotationRequestRepository.findByProductId(productId);
	}
}
