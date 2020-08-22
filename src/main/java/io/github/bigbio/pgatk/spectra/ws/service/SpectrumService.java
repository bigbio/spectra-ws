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
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
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
        PageRequest pageRequest = PageRequest.of(pageParams.getKey(), pageParams.getValue());
//        List<ElasticSpectrum> elasticSpectrums22 = spectrumRepository.findByUsiIn(usis, pageRequest); //not working
        CriteriaQuery query = new CriteriaQuery(new Criteria("_id").in(usis))
                .addSort(Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD))
                .setPageable(pageRequest);

        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        List<ArchiveSpectrum> archiveSpectrums = elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
        return archiveSpectrums;
    }

    public List<ArchiveSpectrum> findByPepSequence(String pepSequence, Tuple<Integer, Integer> pageParams) {

        PageRequest pageRequest = PageRequest.of(pageParams.getKey(), pageParams.getValue(), Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
//        List<ElasticSpectrum> elasticSpectrums = spectrumRepository.findByPepSequenceContaining(pepSequence, pageRequest); //doesn't work for cases like "ABC*XYZ"

        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);

        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        List<ArchiveSpectrum> archiveSpectrums = elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
        return archiveSpectrums;
    }

    public Long findByPepSequenceCount(String pepSequence) {
        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence));
        return elasticsearchRestTemplate.count(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
    }

}

