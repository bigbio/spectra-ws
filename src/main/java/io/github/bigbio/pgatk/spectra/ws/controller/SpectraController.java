package io.github.bigbio.pgatk.spectra.ws.controller;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.service.SpectrumService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/spectra")
@Slf4j
@Tag(name="Spectra")
public class SpectraController {

    private final SpectrumService spectrumService;

    @Autowired
    public SpectraController(SpectrumService spectrumService) {
        this.spectrumService = spectrumService;
    }

    @GetMapping("/findByUsi")
    public Optional<ElasticSpectrum> findByUsi(@Valid @RequestParam String usi)  {
        return spectrumService.getById(usi);
    }

    @PostMapping("/findByMultipleUsis")
    public List<ArchiveSpectrum> findByMultipleUsis(@Valid @RequestBody List<String> usis)  {
        return spectrumService.getByIds(usis);
    }

    @GetMapping("/findByPepSequence")
    public List<ArchiveSpectrum> findByPepSequence(@Valid @RequestParam String pepSequence)  {
        return spectrumService.findByPepSequence(pepSequence);
    }
}

