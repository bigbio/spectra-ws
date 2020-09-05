package io.github.bigbio.pgatk.spectra.ws.model;

import lombok.Data;

import java.util.List;

@Data
public class PtmRequest {
    private String peptideSequenceRegex;
    private List<Integer> positions;
    private PtmKey ptmKey;
    private String ptmValue;
    private List<String> geneAccessions;
    private List<String> proteinAccessions;
}
