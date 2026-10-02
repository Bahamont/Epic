package com.epic.quotation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRequestRepository extends JpaRepository<QuotationRequest, Long> {

	List<QuotationRequest> findByProductId(Long productId);
}
