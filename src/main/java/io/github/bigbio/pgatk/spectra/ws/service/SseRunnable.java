package io.github.bigbio.pgatk.spectra.ws.service;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.INDEX_COORDINATES;

public class SseRunnable implements Runnable {
    private final CriteriaQuery query;
    private final SseEmitter sseEmitter;
    private final Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public SseRunnable(CriteriaQuery query, SseEmitter sseEmitter, Function<List<ElasticSpectrum>, List<ElasticSpectrum>> filterFunc, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.query = query;
        this.sseEmitter = sseEmitter;
        this.filterFunc = filterFunc;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
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

