package com.firstone.greenjangteo.product.service;

import com.firstone.greenjangteo.product.domain.dto.ProductNameDto;
import com.firstone.greenjangteo.product.domain.dto.search.ProductSaveAllRequest;
import com.firstone.greenjangteo.product.domain.dto.search.ProductSearchResponse;
import com.firstone.greenjangteo.product.exception.ProductSearchUnavailableException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 없이 실행할 때(es 프로파일 비활성) 검색 API를 503으로 응답한다.
 */
@Profile("!es")
@Service
public class UnavailableProductSearchService implements ProductSearchService {
    @Override
    public void saveAllProducts(ProductSaveAllRequest productSaveAllRequest) {
        throw new ProductSearchUnavailableException();
    }

    @Override
    public void saveAllProductDocument() {
        throw new ProductSearchUnavailableException();
    }

    @Override
    public List<ProductSearchResponse> findByProductName(String productName, Pageable pageable) {
        throw new ProductSearchUnavailableException();
    }

    @Override
    public List<ProductNameDto> findByStartWithProductName(String productName) {
        throw new ProductSearchUnavailableException();
    }

    @Override
    public List<ProductSearchResponse> findByCategory(String category, Pageable pageable) {
        throw new ProductSearchUnavailableException();
    }
}
