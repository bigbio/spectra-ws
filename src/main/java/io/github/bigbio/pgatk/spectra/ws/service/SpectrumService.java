package io.github.bigbio.pgatk.spectra.ws.service;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.repository.ElasticSearchSpectrumRepository;
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

import io.github.bigbio.pgatk.spectra.ws.utils.Constants;

@Slf4j
@Service
public class SpectrumService {

    private final ElasticSearchSpectrumRepository elasticSearchSpectrumRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Autowired
    public SpectrumService(ElasticSearchSpectrumRepository elasticSearchSpectrumRepository, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticSearchSpectrumRepository = elasticSearchSpectrumRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * Save {@link ElasticSpectrum} in to Elastic indexes
     *
     * @param spectrum
     */
    public void saveSpectrum(ElasticSpectrum spectrum) {
        Assert.notNull(spectrum, "questionLink is null !");
        elasticSearchSpectrumRepository.save(spectrum);
    }

    /**
     * Delete all the spectrum in the index
     */
    public void deleteAll() {
        elasticSearchSpectrumRepository.deleteAll();
    }

    /**
     * Delete by id
     *
     * @param id String accession of spectrum
     */
    public void deleteById(String id) {
        elasticSearchSpectrumRepository.deleteById(id);
    }

    /**
     * Find all the spectra in the index using some pagination
     *
     * @param page Page to index
     * @param size Size.
     * @return
     */
    public List<ElasticSpectrum> findAllPage(int page, int size) {
        return elasticSearchSpectrumRepository.findAll(PageRequest.of(page, size)).toList();
    }

    /**
     * Save batch spectra List
     *
     * @param spectra List of @{@link ElasticSpectrum}
     */
    public void saveAll(List<ElasticSpectrum> spectra) {
        elasticSearchSpectrumRepository.saveAll(spectra);
    }

    /**
     * Get Spectra by Usi accession
     *
     * @param usi
     * @return
     */
    public Optional<ElasticSpectrum> getById(String usi) {
        return elasticSearchSpectrumRepository.findById(usi);
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

        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence))
                .addSort(Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD))
                .setPageable(PageRequest.of(pageParams.getKey(), pageParams.getValue()));

        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, IndexCoordinates.of(Constants.SPECTRA_INDEX_NAME));
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        List<ArchiveSpectrum> archiveSpectrums = elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
        return archiveSpectrums;
    }
}

