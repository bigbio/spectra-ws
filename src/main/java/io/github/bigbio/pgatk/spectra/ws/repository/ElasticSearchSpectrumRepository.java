package io.github.bigbio.pgatk.spectra.ws.repository;

import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ElasticSearchSpectrumRepository extends ElasticsearchRepository<ElasticSpectrum, String> {

    List<ElasticSpectrum> findByIdIn(Collection<String> ids);

}
