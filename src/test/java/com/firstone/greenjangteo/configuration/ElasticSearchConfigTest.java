package com.firstone.greenjangteo.configuration;

import com.firstone.greenjangteo.product.repository.search.ProductSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticSearchConfigTest {
    @DisplayName("자동 구성을 제외했으므로 es 프로파일 설정이 상품 검색 저장소 패키지를 직접 스캔한다.")
    @Test
    void scansProductSearchRepositoryPackage() {
        // when
        EnableElasticsearchRepositories annotation
                = ElasticSearchConfig.class.getAnnotation(EnableElasticsearchRepositories.class);

        // then
        assertThat(annotation.basePackageClasses()).contains(ProductSearchRepository.class);
    }
}
