package io.github.bigbio.pgatk.spectra.ws.controller;

import io.github.bigbio.pgatk.elastic.multiomics.model.ElasticSpectrum;
import io.github.bigbio.pgatk.elastic.multiomics.service.SpectrumService;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.GenericRequest;
import io.github.bigbio.pgatk.spectra.ws.model.PtmKey;
import io.github.bigbio.pgatk.spectra.ws.model.PtmRequest;
import io.github.bigbio.pgatk.spectra.ws.model.Spectrum;
import io.github.bigbio.pgatk.spectra.ws.service.StreamSpectrumService;
import io.github.bigbio.pgatk.spectra.ws.utils.Converters;
import io.github.bigbio.pgatk.spectra.ws.utils.FilterGetByPtmSpectrum;
import io.github.bigbio.pgatk.spectra.ws.utils.GeneralUtils;
import io.github.bigbio.pgatk.spectra.ws.utils.WsUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.*;

@RestController
@Validated
@RequestMapping("/spectra")
@Slf4j
@Tag(name = "Spectra")
public class SpectraController {


    @Autowired
    private SpectrumService spectrumService;

    @Autowired
    private StreamSpectrumService streamSpectrumService;

    @GetMapping("/findByUsi")
    public Optional<Spectrum> findByUsi(@Valid @RequestParam String usi) {
        return spectrumService.getById(usi).map(Converters::elasticToArchiveSpectrum);
    }

    @PostMapping("/stream/findByMultipleUsis")
    public ResponseEntity<ResponseBodyEmitter> findByMultipleUsisStream(@Valid @RequestBody List<String> usis) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(USI_KEYWORD).in(usis)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByMultipleUsis")
    public SseEmitter findByMultipleUsisSse(@Valid @RequestBody List<String> usis) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(USI_KEYWORD).in(usis)).setPageable(pageRequest);
        return streamSpectrumService.getSseEmitter(query, null);
    }

    @GetMapping("/findByPepSequence")
    public List<Spectrum> findByPepSequence(@Valid @RequestParam String peptideSequenceRegex,
                                            @RequestParam(value = "page", defaultValue = "0") Integer page,
                                            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        return spectrumService.findByPepSequence(peptideSequenceRegex, pageParams)
                .stream().map(Converters::elasticToArchiveSpectrum)
                .collect(Collectors.toList());
    }

    @GetMapping("/findByPepSequence/count")
    public Long findByPepSequenceCount(@Valid @RequestParam String peptideSequenceRegex) {

        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        return spectrumService.findByPepSequenceCount(peptideSequenceRegex);
    }

    @GetMapping(path = "/stream/findByPepSequence")
    public ResponseEntity<ResponseBodyEmitter> findByPepSequenceStream(@Valid @RequestParam String peptideSequenceRegex) {
        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @GetMapping(path = "/sse/findByPepSequence")
    public SseEmitter findByPepSequenceSse(@Valid @RequestParam String peptideSequenceRegex) {
        WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex)).setPageable(pageRequest);
        return streamSpectrumService.getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByProteinAccessions")
    public ResponseEntity<ResponseBodyEmitter> findByProteinAccessionStream(@Valid @RequestBody List<String> proteinAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByProteinAccessions")
    public SseEmitter findByProteinAccessionSse(@Valid @RequestBody List<String> proteinAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions)).setPageable(pageRequest);
        return streamSpectrumService.getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByGeneAccessions")
    public ResponseEntity<ResponseBodyEmitter> findByGeneAccessionStream(@Valid @RequestBody List<String> geneAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions)).setPageable(pageRequest);
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByGeneAccessions")
    public SseEmitter findByGeneAccessionSse(@Valid @RequestBody List<String> geneAccessions) {
        PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
        CriteriaQuery query = new CriteriaQuery(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions)).setPageable(pageRequest);
        return streamSpectrumService.getSseEmitter(query, null);
    }

    @PostMapping(path = "/stream/findByPtm")
    public ResponseEntity<ResponseBodyEmitter> findByPtmStream(@RequestBody PtmRequest ptmRequest) {
        CriteriaQuery query = getFindByPtmQuery(ptmRequest);
        FilterGetByPtmSpectrum filterFunc = new FilterGetByPtmSpectrum(ptmRequest.getPtmKey(), ptmRequest.getPtmValue(), ptmRequest.getPositions());
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, filterFunc);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByPtm")
    public SseEmitter findByPtmSse(@RequestBody PtmRequest ptmRequest) {
        CriteriaQuery query = getFindByPtmQuery(ptmRequest);
        FilterGetByPtmSpectrum filterFunc = new FilterGetByPtmSpectrum(ptmRequest.getPtmKey(), ptmRequest.getPtmValue(), ptmRequest.getPositions());
        return streamSpectrumService.getSseEmitter(query, filterFunc);
    }

    @PostMapping("/findByGenericRequest")
    public List<Spectrum> findByGenericRequest(@RequestBody GenericRequest genericRequest,
                                                      @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                      @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        CriteriaQuery query = getQueryForGenericRequest(genericRequest, false);
        return spectrumService.findByQuery(query, pageParams).stream()
                .map(Converters::elasticToArchiveSpectrum)
                .collect(Collectors.toList());
    }
//
    @PostMapping("/findByGenericRequest/count")
    public Long findByGenericRequestCount(@RequestBody GenericRequest genericRequest) {
        CriteriaQuery query = getQueryForGenericRequest(genericRequest, false);
        return spectrumService.getCountForQuery(query);
    }

    @PostMapping(path = "/stream/findByGenericRequest")
    public ResponseEntity<ResponseBodyEmitter> findByGenericRequestStream(@RequestBody GenericRequest genericRequest) {
        CriteriaQuery query = getQueryForGenericRequest(genericRequest, true);
        ResponseBodyEmitter emitter = streamSpectrumService.getStreamEmitter(query, null);
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @PostMapping(path = "/sse/findByGenericRequest")
    public SseEmitter findByGenericRequestSee(@RequestBody GenericRequest genericRequest) {
        CriteriaQuery query = getQueryForGenericRequest(genericRequest, true);
        return streamSpectrumService.getSseEmitter(query, null);
    }

    private CriteriaQuery getQueryForGenericRequest(GenericRequest genericRequest, boolean isStreamOrSse) {
        String peptideSequenceRegex = genericRequest.getPeptideSequenceRegex();
        Criteria criteria = new Criteria();
        if (peptideSequenceRegex != null && !peptideSequenceRegex.isEmpty()) {
            WsUtils.validatePeptideSeqRegex(peptideSequenceRegex);
            criteria = criteria.and(new Criteria(PEPTIDE_SEQUENCE).expression(peptideSequenceRegex));
        }

        List<String> geneAccessions = genericRequest.getGeneAccessions();
        if (geneAccessions != null && !geneAccessions.isEmpty()) {
            criteria = criteria.and(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions));
        }

        List<String> proteinAccessions = genericRequest.getProteinAccessions();
        if (proteinAccessions != null && !proteinAccessions.isEmpty()) {
            criteria = criteria.and(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions));
        }

        GenericRequest.Ptm ptm = genericRequest.getPtm();
        if (ptm != null) {
            PtmKey ptmKey = ptm.getPtmKey();
            String ptmValue = ptm.getPtmValue();
            if (ptmKey == null || GeneralUtils.isEmpty(ptmValue)) {
                throw new IllegalArgumentException("ptmKey should be one of these: 'name, accession, mass' and ptmValue should be it's corresponding value");
            }
            criteria = criteria.and(new Criteria(ptmKey.getElastname()).is(ptmValue));
        }

        List<Criteria> criteriaChain = criteria.getCriteriaChain();
        if (criteriaChain.isEmpty()) {
            throw new IllegalArgumentException("Any one filter is mandatory");
        }

        if (isStreamOrSse) {
            PageRequest pageRequest = PageRequest.of(0, MAX_PAGINATION_SIZE, Sort.by(Sort.Direction.ASC, USI_KEYWORD));
            return new CriteriaQuery(criteria).setPageable(pageRequest);
        }

        return new CriteriaQuery(criteria);
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

        if (positions != null && positions.size() > 0) {
            int i = 0;
            Criteria posCriteria = new Criteria(PTM_MODIFICATION_POSITION_MAP_KEY).is(positions.get(i));
            while (++i < positions.size()) {
                posCriteria = posCriteria.or(new Criteria(PTM_MODIFICATION_POSITION_MAP_KEY).is(positions.get(i)));
            }
            criteria = criteria.and(posCriteria);
        }

        List<String> geneAccessions = ptmRequest.getGeneAccessions();
        if (geneAccessions != null && !geneAccessions.isEmpty()) {
            criteria = criteria.and(new Criteria(GENE_ACCESSIONS_KEYWORD).in(geneAccessions));
        }

        List<String> proteinAccessions = ptmRequest.getProteinAccessions();
        if (proteinAccessions != null && !proteinAccessions.isEmpty()) {
            criteria = criteria.and(new Criteria(PROTEIN_ACCESSIONS_KEYWORD).in(proteinAccessions));
        }

        return new CriteriaQuery(criteria).setPageable(pageRequest);
    }
}

