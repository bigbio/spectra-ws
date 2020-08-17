package io.github.bigbio.pgatk.spectra.ws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.repository.SpectrumRepositoryStream;
import io.github.bigbio.pgatk.spectra.ws.service.SpectrumService;
import io.github.bigbio.pgatk.spectra.ws.utils.Constants;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import io.github.bigbio.pgatk.spectra.ws.utils.WsUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
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

    @PostMapping("/findByMultipleUsis")
    public List<ArchiveSpectrum> findByMultipleUsis(@Valid @RequestBody List<String> usis,
                                                    @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                    @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        return spectrumService.getByIds(usis, pageParams);
    }

    @GetMapping("/findByPepSequence")
    public List<ArchiveSpectrum> findByPepSequence(@Valid @RequestParam String pepSequence,
                                                   @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                   @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        return spectrumService.findByPepSequence(pepSequence, pageParams);
    }

//    @GetMapping(path = "/stream/findByPepSequence")
//    public ResponseEntity<ResponseBodyEmitter> findByPepSequenceStream(@Valid @RequestParam String pepSequence) {
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
//        emitterFunc(pepSequence, emitter, false);
//        return new ResponseEntity(emitter, HttpStatus.OK);
//    }
//
//    private void emitterFunc(String pepSequence, ResponseBodyEmitter emitter, boolean isSse) {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.execute(() -> {
//            final String NEWLINE = "\n";
//            PageRequest pageRequest = PageRequest.of(0, Constants.MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
//            CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);
//            int scrollTimeInMillis = 60000;
//            List<String> scrollIds = new ArrayList<>();
//            SearchScrollHits<ElasticSpectrum> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
//            String scrollId = scroll.getScrollId();
//            scrollIds.add(scrollId);
//            AtomicLong id = new AtomicLong();
//            while (scroll.hasSearchHits()) {
//                scroll.getSearchHits().forEach(s -> {
//                    ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s.getContent());
//                    try {
//                        if (isSse) {
//                            emitter.send(SseEmitter.event()
//                                    .id(String.valueOf(id.incrementAndGet()))
//                                    .name("Spectrum")
//                                    .data(archiveSpectrum));
//                        } else {
//                            emitter.send(archiveSpectrum, MediaType.APPLICATION_JSON);
//                        }
//                        if (!isSse) {
//                            emitter.send(NEWLINE);
//                        }
//                    } catch (Exception ex) {
//                        log.error(ex.getMessage(), ex);
//                        emitter.completeWithError(ex);
//                    }
//                });
//                scroll = elasticsearchRestTemplate.searchScrollContinue(scrollId, scrollTimeInMillis, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
//                scrollId = scroll.getScrollId();
//                scrollIds.add(scrollId);
//            }
//            emitter.complete();
//            elasticsearchRestTemplate.searchScrollClear(scrollIds);
//        });
//        executor.shutdown();
//    }

    @GetMapping(path = "/sse/findByPepSequence")
    public SseEmitter findByPepSequenceSse(@Valid @RequestParam String pepSequence) {
        PageRequest pageRequest = PageRequest.of(0, Constants.MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);
        SseEmitter sseEmitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SseRunnable(query, sseEmitter));
        executor.shutdown();
        return sseEmitter;
    }

    @PostMapping(path = "/sse/findByProteinAccessions")
    public SseEmitter findByProteinAccessionSse(@Valid @RequestBody List<String> proteinAccessions) {
        PageRequest pageRequest = PageRequest.of(0, Constants.MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria("proteinAccessions.keyword").in(proteinAccessions)).setPageable(pageRequest);
        SseEmitter sseEmitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SseRunnable(query, sseEmitter));
        executor.shutdown();
        return sseEmitter;
    }

    @PostMapping(path = "/sse/findByGeneAccessions")
    public SseEmitter findByGeneAccessionSse(@Valid @RequestBody List<String> geneAccessions) {
        PageRequest pageRequest = PageRequest.of(0, Constants.MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria("geneAccessions.keyword").in(geneAccessions)).setPageable(pageRequest);
        SseEmitter sseEmitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SseRunnable(query, sseEmitter));
        executor.shutdown();
        return sseEmitter;
    }

    class SseRunnable implements Runnable {
        private final CriteriaQuery query;
        private final  SseEmitter sseEmitter;

        SseRunnable(CriteriaQuery query, SseEmitter sseEmitter) {
            this.query = query;
            this.sseEmitter = sseEmitter;
        }

        @Override
        public void run() {
            int scrollTimeInMillis = 60000;
            List<String> scrollIds = new ArrayList<>();
            SearchScrollHits<ElasticSpectrum> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
            String scrollId = scroll.getScrollId();
            scrollIds.add(scrollId);
            AtomicLong id = new AtomicLong();
            while (scroll.hasSearchHits()) {
                scroll.getSearchHits().forEach(s -> {
                    ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s.getContent());
                    try {
                        SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                                .id(String.valueOf(id.incrementAndGet()))
                                .name("Spectrum")
                                .data(archiveSpectrum);
                        sseEmitter.send(sseEventBuilder);

                    } catch (Exception ex) {
//                        log.error(ex.getMessage(), ex);
                        sseEmitter.completeWithError(ex);
                    }
                });
                scroll = elasticsearchRestTemplate.searchScrollContinue(scrollId, scrollTimeInMillis, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
                scrollId = scroll.getScrollId();
                scrollIds.add(scrollId);
            }
            SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                    .id(String.valueOf(id.incrementAndGet()))
                    .name("COMPLETE");
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

