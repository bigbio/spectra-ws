package io.github.bigbio.pgatk.spectra.ws.repository;

import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchSpectrumRepository extends ElasticsearchRepository<ElasticSpectrum, String> {

}
