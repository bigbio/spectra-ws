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

    @GetMapping(path = "/stream/findByPepSequence")
    public ResponseEntity<ResponseBodyEmitter> findByPepSequenceStream(@Valid @RequestParam String pepSequence) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        emitterFunc(pepSequence, emitter, true);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

//    private void emitterFunc(String pepSequence, ResponseBodyEmitter emitter, boolean addNewline) {
//        Stream<ElasticSpectrum> esStream = spectrumRepositoryStream.findByPepSequenceContaining(pepSequence);
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.execute(() -> {
//            final String NEWLINE = "\n";
//            esStream.forEach(s -> {
//                ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s);
//                try {
//                    emitter.send(archiveSpectrum, MediaType.APPLICATION_JSON);
//                    if(addNewline) {
//                      emitter.send(NEWLINE);
//                    }
//                } catch (Exception ex) {
//                    emitter.completeWithError(ex);
//                }
//            });
//            emitter.complete();
//        });
//        executor.shutdown();
//    }

    private void emitterFunc(String pepSequence, ResponseBodyEmitter emitter, boolean addNewline) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            final String NEWLINE = "\n";
            PageRequest pageRequest = PageRequest.of(0, Constants.MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, Constants.USI_KEYWORD));
            CriteriaQuery query = new CriteriaQuery(new Criteria("pepSequence").expression(pepSequence)).setPageable(pageRequest);
            int scrollTimeInMillis = 60000;
            List<String> scrollIds = new ArrayList<>();
            SearchScrollHits<ElasticSpectrum> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, query, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
            String scrollId = scroll.getScrollId();
            scrollIds.add(scrollId);
            while (scroll.hasSearchHits()) {
                scroll.getSearchHits().forEach(s -> {
                    ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s.getContent());
                    try {
                        emitter.send(archiveSpectrum, MediaType.APPLICATION_JSON);
                        if(addNewline) {
                            emitter.send(NEWLINE);
                        }
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                });
                scroll = elasticsearchRestTemplate.searchScrollContinue(scrollId, scrollTimeInMillis, ElasticSpectrum.class, Constants.INDEX_COORDINATES);
                scrollId = scroll.getScrollId();
                scrollIds.add(scrollId);
            }
            emitter.complete();
            elasticsearchRestTemplate.searchScrollClear(scrollIds);
        });
        executor.shutdown();
    }

    @GetMapping(path = "/sse/findByPepSequence")
    public SseEmitter findByPepSequenceSse(@Valid @RequestParam String pepSequence) {
        SseEmitter sseEmitter = new SseEmitter();
        emitterFunc(pepSequence, sseEmitter, false);
        return sseEmitter;
    }

//    @GetMapping(path = "/stream2/findByPepSequence")
//    public ResponseEntity<StreamingResponseBody> findByPepSequenceStream2(@Valid @RequestParam String pepSequence) {
//        Stream<ElasticSpectrum> stream = spectrumRepositoryStream.findByPepSequenceContaining(pepSequence);
//        final String NEWLINE = "\n";
//        StreamingResponseBody streamOut = out -> stream.forEach(s -> {
//            ArchiveSpectrum archiveSpectrum = Converters.elasticToArchiveSpectrum(s);
//            try {
//                String asString = objectMapper.writeValueAsString(archiveSpectrum) + NEWLINE;
//                out.write(asString.getBytes());
//            } catch (IOException e) {
//                throw new IllegalStateException(e.getMessage(), e);
//            }
//        });
//        return new ResponseEntity(streamOut, HttpStatus.OK);
//    }

//    @GetMapping(path = "/plainstream/test")
//    public ResponseEntity<StreamingResponseBody> test1() {
//        Integer a[] = new Integer[9999999];
//        Arrays.fill(a, 10);
//        List<Integer> strings = Arrays.asList(a);
//        StreamingResponseBody streamOut = out -> strings.forEach(s -> {
//            ArchiveSpectrum archiveSpectrum = new ArchiveSpectrum();
//            try {
//                String asString = objectMapper.writeValueAsString(archiveSpectrum) + "\n";
//                asString = "1";
//                out.write(asString.getBytes());
//            } catch (Exception e) {
//                throw new IllegalStateException(e.getMessage(), e);
//            }
//        });
//        System.out.println("i am out 1");
//        return new ResponseEntity(streamOut, HttpStatus.OK);
//    }
//
//    @GetMapping(path = "/jsonstream/test")
//    public ResponseEntity<ResponseBodyEmitter> test2() {
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
//        emitterTestFunc(emitter);
//        return new ResponseEntity(emitter, HttpStatus.OK);
//    }
//
//    @GetMapping(path = "/sse/test")
//    public SseEmitter test3() {
//        SseEmitter emitter = new SseEmitter();
//        emitterTestFunc(emitter);
//        return emitter;
//    }
//
//    private void emitterTestFunc(ResponseBodyEmitter emitter) {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Integer a[] = new Integer[99999];
//        Arrays.fill(a, 10);
//        List<Integer> strings = Arrays.asList(a);
//        executor.execute(() -> {
//            strings.forEach(s -> {
//                ArchiveSpectrum archiveSpectrum = new ArchiveSpectrum();
//                try {
//                    emitter.send(archiveSpectrum, MediaType.APPLICATION_JSON);
//                    emitter.send("\n");
//                } catch (Exception ex) {
//                    emitter.completeWithError(ex);
//                }
//            });
//            emitter.complete();
//        });
//        executor.shutdown();
//        System.out.println("i am out 2");
//    }
}

