package io.github.bigbio.pgatk.spectra.ws.service;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.repository.SpectrumRepository;
import io.github.bigbio.pgatk.spectra.ws.utils.Constants;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import io.github.bigbio.pgatk.spectra.ws.utils.FilterGetByPtmSpectrum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.INDEX_COORDINATES;

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

        return getArchiveSpectrums(query);
    }

    public List<ArchiveSpectrum> findByPepSequence(String pepSequence, Tuple<Integer, Integer> pageParams) {
        PageRequest pageRequest = PageRequest.of(pageParams.getKey(), pageParams.getValue(), Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
//        List<ElasticSpectrum> elasticSpectrums = spectrumRepository.findByPepSequenceContaining(pepSequence, pageRequest); //doesn't work for cases like "ABC*XYZ"
        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);
        return getArchiveSpectrums(query);
    }

    public Long findByPepSequenceCount(String pepSequence) {
        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence));
        return elasticsearchRestTemplate.count(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
    }

    public List<ArchiveSpectrum> findByQuery(CriteriaQuery query, Tuple<Integer, Integer> pageParams) {
        if (pageParams != null) {
            PageRequest pageRequest = PageRequest.of(pageParams.getKey(), pageParams.getValue(), Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
            query = query.setPageable(pageRequest);
        }
        return getArchiveSpectrums(query);
    }

    private List<ArchiveSpectrum> getArchiveSpectrums(CriteriaQuery query) {
        SearchHits<ElasticSpectrum> searches = elasticsearchRestTemplate.search(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
        List<ElasticSpectrum> elasticSpectrums = searches.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return elasticSpectrums.stream().map(Converters::elasticToArchiveSpectrum).collect(Collectors.toList());
    }

    public Long getCountForQuery(CriteriaQuery query) {
        return elasticsearchRestTemplate.count(query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
    }

    public SseEmitter getSseEmitter(CriteriaQuery query, FilterGetByPtmSpectrum filterFunc) {
        SseEmitter sseEmitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SseRunnable(query, sseEmitter, filterFunc, elasticsearchRestTemplate));
        executor.shutdown();
        return sseEmitter;
    }

    public ResponseBodyEmitter getStreamEmitter(CriteriaQuery query, Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            final String NEWLINE = "\n";
            int scrollTimeInMillis = 60000;
            List<String> scrollIds = new ArrayList<>();
            SearchScrollHits<ElasticSpectrum> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, query, ElasticSpectrum.class, INDEX_COORDINATES);
            String scrollId = scroll.getScrollId();
            scrollIds.add(scrollId);
            while (scroll.hasSearchHits()) {
                List<SearchHit<ElasticSpectrum>> searchHits = scroll.getSearchHits();
                List<ElasticSpectrum> elasticSpectrums = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());

                if (filterFunc != null) {
                    elasticSpectrums = filterFunc.apply(elasticSpectrums);
                }

                elasticSpectrums.forEach(s -> {
                    ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s);
                    try {
                        emitter.send(archiveSpectrum, MediaType.APPLICATION_JSON);
                        emitter.send(NEWLINE);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        emitter.completeWithError(ex);
                    }
                });
                scroll = elasticsearchRestTemplate.searchScrollContinue(scrollId, scrollTimeInMillis, ElasticSpectrum.class, INDEX_COORDINATES);
                scrollId = scroll.getScrollId();
                scrollIds.add(scrollId);
            }
            emitter.complete();
            elasticsearchRestTemplate.searchScrollClear(scrollIds);
        });
        executor.shutdown();
        return emitter;
    }
}

