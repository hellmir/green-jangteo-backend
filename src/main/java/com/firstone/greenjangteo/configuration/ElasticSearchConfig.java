package com.firstone.greenjangteo.configuration;

import com.firstone.greenjangteo.product.repository.search.ProductSearchRepository;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Profile("es")
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = ProductSearchRepository.class)
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${EC2_IP_FOR_GREEN_JANGTEO}")
    private String elasticSearchHost;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticSearchHost + ":9200")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}

