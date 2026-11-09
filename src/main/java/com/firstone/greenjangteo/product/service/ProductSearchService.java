package com.firstone.greenjangteo.product.service;

import com.firstone.greenjangteo.product.domain.dto.ProductNameDto;
import com.firstone.greenjangteo.product.domain.dto.search.ProductSaveAllRequest;
import com.firstone.greenjangteo.product.domain.dto.search.ProductSearchResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {
    void saveAllProducts(ProductSaveAllRequest productSaveAllRequest);

    void saveAllProductDocument();

    List<ProductSearchResponse> findByProductName(String productName, Pageable pageable);

    List<ProductNameDto> findByStartWithProductName(String productName);

    List<ProductSearchResponse> findByCategory(String category, Pageable pageable);
}
