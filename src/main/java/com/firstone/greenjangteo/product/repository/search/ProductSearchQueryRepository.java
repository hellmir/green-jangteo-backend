package com.firstone.greenjangteo.product.repository.search;

import com.firstone.greenjangteo.product.domain.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;


@Profile("es")
@Repository
@RequiredArgsConstructor
public class ProductSearchQueryRepository {

    private final ElasticsearchOperations operations;

    public List<ProductDocument> findByStartWithProductName(String productName) {
        Criteria criteria = Criteria.where("name").startsWith(productName);
        Query query = new CriteriaQuery(criteria);
        SearchHits<ProductDocument> search = operations.search(query, ProductDocument.class);
        return search.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    public List<ProductDocument> findByCategory(String category, Pageable pageable) {
        Criteria criteria = Criteria.where("category.firstCategory").startsWith(category)
                .or(Criteria.where("category.secondCategory").startsWith(category));
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ProductDocument> search = operations.search(query, ProductDocument.class);
        return search.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }
}