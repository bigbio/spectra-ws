package io.github.bigbio.pgatk.spectra.ws.model;

import lombok.Data;

import java.util.List;

@Data
public class PtmRequest {
    String peptideSequenceRegex;
    List<Integer> positions;
    PtmKey ptmKey;
    String ptmValue;
}
