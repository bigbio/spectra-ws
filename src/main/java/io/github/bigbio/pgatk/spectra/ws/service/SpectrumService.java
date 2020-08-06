package io.github.bigbio.pgatk.spectra.ws.service;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.repository.SpectrumRepository;
import io.github.bigbio.pgatk.spectra.ws.utils.Constants;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpectrumService {

    private final SpectrumRepository spectrumRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Autowired
    public SpectrumService(SpectrumRepository spectrumRepository, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.spectrumRepository = spectrumRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * Get Spectra by Usi accession
     *
     * @param usi
     * @return
     */
    public Optional<ElasticSpectrum> getById(String usi) {
        return spectrumRepository.findById(usi);
    }

    public List<ArchiveSpectrum> getByIds(List<String> usis, Tuple<Integer, Integer> pageParams) {

//        List<ElasticSpectrum> elasticSpectrums = elasticSearchSpectrumRepository.findByIdIn(usis);
        CriteriaQuery query = new CriteriaQuery(new Criteria("_id").in(usis))
                .addSort(Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD))
                .setPageable(PageRequest.of(pageParams.getKey(), pageParams.getValue()));

        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, IndexCoordinates.of(Constants.SPECTRA_INDEX_NAME));
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        List<ArchiveSpectrum> archiveSpectrums = elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
        return archiveSpectrums;
    }

    public List<ArchiveSpectrum> findByPepSequence(String pepSequence, Tuple<Integer, Integer> pageParams) {

        PageRequest pageRequest = PageRequest.of(pageParams.getKey(), pageParams.getValue(), Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
//        List<ElasticSpectrum> elasticSpectrums = spectrumRepository.findByPepSequenceLike(pepSequence, pageRequest); //doesn't work for cases like "ABC*XYZ"

        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);

        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, IndexCoordinates.of(Constants.SPECTRA_INDEX_NAME));
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        List<ArchiveSpectrum> archiveSpectrums = elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
        return archiveSpectrums;
    }
}

