package io.github.bigbio.pgatk.spectra.ws.repository;

import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface SpectrumRepositoryStream extends ElasticsearchRepository<ElasticSpectrum, String> {
    Stream<ElasticSpectrum> findByPepSequenceLike(String pep);
}
