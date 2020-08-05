package io.github.bigbio.pgatk.spectra.ws.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "io.github.bigbio.pgatk.spectra.ws.repository")
public class ElasticSearchConfig {

    @Value("${elasticsearch.host_url}")
    private String esHost;

    @Bean
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restHighLevelClient());
    }

    private RestHighLevelClient restHighLevelClient() {
        String[] split = esHost.split(",");
        HttpHost[] httpHostArray = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String item = split[i];
            httpHostArray[i] = new HttpHost(item.split(":")[0],
                    Integer.parseInt(item.split(":")[1]), "http");
        }
        RestClientBuilder builder = RestClient.builder(httpHostArray);
        return new RestHighLevelClient(builder);
    }
}
