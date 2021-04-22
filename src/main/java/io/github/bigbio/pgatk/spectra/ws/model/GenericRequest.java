package io.github.bigbio.pgatk.spectra.ws.model;

import lombok.Data;

import java.util.List;

@Data
public class GenericRequest {
    private String peptideSequenceRegex;
    private Ptm ptm;
    private List<String> geneAccessions;
    private List<String> proteinAccessions;

    @Data
    public class Ptm {
        private PtmKey ptmKey;
        private String ptmValue;
    }
}
