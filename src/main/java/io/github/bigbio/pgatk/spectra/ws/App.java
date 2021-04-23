package io.github.bigbio.pgatk.spectra.ws;

import io.github.bigbio.pgatk.elastic.multiomics.config.ElasticSearchConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication()
@Import({ ElasticSearchConfig.class})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
