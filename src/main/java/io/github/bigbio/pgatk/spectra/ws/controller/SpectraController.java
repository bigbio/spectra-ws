package io.github.bigbio.pgatk.spectra.ws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.PtmKey;
import io.github.bigbio.pgatk.spectra.ws.model.PtmRequest;
import io.github.bigbio.pgatk.spectra.ws.repository.SpectrumRepositoryStream;
import io.github.bigbio.pgatk.spectra.ws.service.SpectrumService;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import io.github.bigbio.pgatk.spectra.ws.utils.FilterGetByPtmSpectrum;
import io.github.bigbio.pgatk.spectra.ws.utils.GeneralUtils;
import io.github.bigbio.pgatk.spectra.ws.utils.WsUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.*;

@RestController
@Validated
@RequestMapping("/spectra")
@Slf4j
@Tag(name = "Spectra")
public class SpectraController {

    private final SpectrumService spectrumService;
    private final ObjectMapper objectMapper;
    private final SpectrumRepositoryStream spectrumRepositoryStream;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public SpectraController(SpectrumService spectrumService, ObjectMapper objectMapper, SpectrumRepositoryStream spectrumRepositoryStream, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.spectrumService = spectrumService;
        this.objectMapper = objectMapper;
        this.spectrumRepositoryStream = spectrumRepositoryStream;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @GetMapping("/findByUsi")
    public Optional<ElasticSpectrum> findByUsi(@Valid @RequestParam String usi) {
        return spectrumService.getById(usi);
    }

//    @PostMapping("/findByMultipleUsis")
//    public List<ArchiveSpectrum> findByMultipleUsis(@Valid @RequestBody List<String> usis,
//                                                    @RequestParam(value = "page", defaultValue = "0") Integer page,
//                                                    @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
//        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        return spectrumService.getByIds(usis, pageParams);
//    }

    @PostMapping("/stream/findByMultipleUsis")
    public ResponseEntity<ResponseBodyEmitter> findByMultipleUsisStream(@Valid @RequestBody List<String> usis) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(USI_KEYWORD).in(usis)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByMultipleUsis")
    public SseEmitter findByMultipleUsisSse(@Valid @RequestBody List<String> usis) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(USI_KEYWORD).in(usis)).setPageable(pageRequest);
        return getSseEmitter(query, null);
    }

//    @GetMapping("/findByPepSequence")
//    public List<ArchiveSpectrum> findByPepSequence(@Valid @RequestParam String peptideSequenceRegex,
//                                                   @RequestParam(value = "page", defaultValue = "0") Integer page,
//                                                   @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
//
//        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
//        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        return spectrumService.findByPepSequence(peptideSequenceRegex, pageParams);
//    }

    @GetMapping(path = "/stream/findByPepSequence")
    public ResponseEntity<ResponseBodyEmitter> findByPepSequenceStream(@Valid @RequestParam String peptideSequenceRegex) {
        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @GetMapping(path = "/sse/findByPepSequence")
    public SseEmitter findByPepSequenceSse(@Valid @RequestParam String peptideSequenceRegex) {
        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex)).setPageable(pageRequest);
        return getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByProteinAccessions")
    public ResponseEntity<ResponseBodyEmitter> findByProteinAccessionStream(@Valid @RequestBody List<String> proteinAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByProteinAccessions")
    public SseEmitter findByProteinAccessionSse(@Valid @RequestBody List<String> proteinAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions)).setPageable(pageRequest);
        return getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByGeneAccessions")
    public ResponseEntity<ResponseBodyEmitter> findByGeneAccessionStream(@Valid @RequestBody List<String> geneAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByGeneAccessions")
    public SseEmitter findByGeneAccessionSse(@Valid @RequestBody List<String> geneAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions)).setPageable(pageRequest);
        return getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByPtm")
    public ResponseEntity<ResponseBodyEmitter> findByPtmStream(@RequestBody PtmRequest ptmRequest) {
        CriteriaQuery query = getFindByPtmQuery(ptmRequest);
        FilterGetByPtmSpectrum filterFunc = new FilterGetByPtmSpectrum(ptmRequest.getPtmKey(), ptmRequest.getPtmValue(), ptmRequest.getPositions());
        ResponseBodyEmitter emitter = getStreamEmitter(query, filterFunc);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByPtm")
    public SseEmitter findByPtmSse(@RequestBody PtmRequest ptmRequest) {
        CriteriaQuery query = getFindByPtmQuery(ptmRequest);
        FilterGetByPtmSpectrum filterFunc = new FilterGetByPtmSpectrum(ptmRequest.getPtmKey(), ptmRequest.getPtmValue(), ptmRequest.getPositions());

        return getSseEmitter(query, filterFunc);
    }

    private CriteriaQuery getFindByPtmQuery(PtmRequest ptmRequest) {
        String peptideSequenceRegex = ptmRequest.getPeptideSequenceRegex();
        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        PtmKey ptmKey = ptmRequest.getPtmKey();
        String ptmValue = ptmRequest.getPtmValue();
        List<Integer> positions = ptmRequest.getPositions();
        if (ptmKey == null || GeneralUtils.isEmpty(ptmValue)) {
            throw new IllegalArgumentException("ptmKey should be one of these: 'name, accession, mass' and ptmValue should be it's corresponding value");
        }
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        Criteria criteria = new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex)
                .and(new Criteria(ptmKey.getElastname()).is(ptmValue));
        Criteria posCriteria;
        if (positions != null && positions.size() > 0) {
            int i = 0;
            posCriteria = new Criteria(PTM_MODIFICATION_POSITION_MAP_KEY).is(positions.get(i));
            while (++i < positions.size()) {
                posCriteria = posCriteria.or(new Criteria(PTM_MODIFICATION_POSITION_MAP_KEY).is(positions.get(i)));
            }
            criteria = criteria.and(posCriteria);
        }
        return new CriteriaQuery(criteria).setPageable(pageRequest);
    }

    private SseEmitter getSseEmitter(CriteriaQuery query, FilterGetByPtmSpectrum filterFunc) {
        SseEmitter sseEmitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SseRunnable(query, sseEmitter, filterFunc));
        executor.shutdown();
        return sseEmitter;
    }

    private ResponseBodyEmitter getStreamEmitter(CriteriaQuery query, Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc) {
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

    class SseRunnable implements Runnable {
        private final CriteriaQuery query;
        private final SseEmitter sseEmitter;
        private final Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc;

        SseRunnable(CriteriaQuery query, SseEmitter sseEmitter, Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc) {
            this.query = query;
            this.sseEmitter = sseEmitter;
            this.filterFunc = filterFunc;
        }

        @Override
        public void run() {
            int scrollTimeInMillis = 60000;
            List<String> scrollIds = new ArrayList<>();
            SearchScrollHits<ElasticSpectrum> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, query, ElasticSpectrum.class, INDEX_COORDINATES);
            String scrollId = scroll.getScrollId();
            scrollIds.add(scrollId);
            AtomicLong id = new AtomicLong();
            while (scroll.hasSearchHits()) {
                List<SearchHit<ElasticSpectrum>> searchHits = scroll.getSearchHits();
                List<ElasticSpectrum> elasticSpectrums = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());

                if (filterFunc != null) {
                    elasticSpectrums = filterFunc.apply(elasticSpectrums);
                }

                elasticSpectrums.forEach(s -> {
                    ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s);
                    try {
                        SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                                .id(String.valueOf(id.incrementAndGet()))
                                .name("spectrum")
                                .data(archiveSpectrum);
                        sseEmitter.send(sseEventBuilder);

                    } catch (Exception ex) {
//                        log.error(ex.getMessage(), ex);
                        sseEmitter.completeWithError(ex);
                    }
                });
                scroll = elasticsearchRestTemplate.searchScrollContinue(scrollId, scrollTimeInMillis, ElasticSpectrum.class, INDEX_COORDINATES);
                scrollId = scroll.getScrollId();
                scrollIds.add(scrollId);
            }
            SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                    .id(String.valueOf(id.incrementAndGet()))
                    .name("done")
                    .data("");
            try {
                sseEmitter.send(sseEventBuilder);
            } catch (Exception ex) {
//                log.error(ex.getMessage(), ex);
                sseEmitter.completeWithError(ex);
            }
            sseEmitter.complete();
            elasticsearchRestTemplate.searchScrollClear(scrollIds);
        }
    }
}

