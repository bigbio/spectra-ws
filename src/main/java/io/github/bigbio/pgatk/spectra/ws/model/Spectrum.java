package io.github.bigbio.pgatk.spectra.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.bigbio.pgatk.io.pride.AccessionLocalization;
import io.github.bigbio.pgatk.io.pride.CvParam;
import io.github.bigbio.pgatk.io.pride.GeneCoordinates;
import io.github.bigbio.pgatk.io.pride.IdentifiedModification;
import io.github.bigbio.pgatk.io.utils.Tuple;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@JsonRootName("Spectrum")
@JsonTypeName("Spectrum")
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Spectrum {

    @JsonProperty("usi")
    private String usi;

    @JsonProperty("pepSequence")
    private String pepSequence;

    @JsonProperty("peptidoform")
    private String peptidoform;

    @JsonProperty("proteinAccessions")
    private Set<String> proteinAccessions;

    @JsonProperty("geneAccessions")
    private Set<String> geneAccessions;

    @JsonProperty("proteinLocalizations")
    private Set<AccessionLocalization> proteinLocalizations;

    @JsonProperty("geneLocalizations")
    private Set<GeneCoordinates> geneLocalizations;

    @JsonProperty("organism")
    private String organism;

    @JsonProperty("sample")
    private List<Tuple<String, String>> sample;

    @JsonProperty("biologicalAnnotations")
    Set<String> biologicalAnnotations;

    @JsonProperty("precursorMz")
    private double precursorMz;

    @JsonProperty("precursorCharge")
    private int precursorCharge;

    @JsonProperty("modifications")
    private List<IdentifiedModification> modifications;

    @JsonProperty("modificationNames")
    private Set<String> modificationNames;

    @JsonProperty("modificationAccessions")
    private Set<String> modificationAccessions;

    @JsonProperty("masses")
    private List<Double> masses;

    @JsonProperty("intensities")
    private List<Double> intensities;

    @JsonProperty("retentionTime")
    Double retentionTime;

    @JsonProperty("msLevel")
    int msLevel;

    @JsonProperty("missedCleavages")
    Integer missedCleavages;

    @JsonProperty("qualityScores")
    private Set<CvParam> qualityScores;

    @JsonProperty("msAnnotations")
    Set<CvParam> msAnnotations;

    @JsonProperty("pxAccession")
    private String pxAccession;

    @JsonProperty("isDecoy")
    private Boolean isDecoy;

    @JsonProperty("peptideIntensity")
    private Double peptideIntensity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Collection<String> getModificationNames() {
        return modifications.stream().map(x -> x.getModificationCvTerm().getName()).collect(Collectors.toList());
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Integer getNumberModifiedSites() {
        return modifications.size();
    }

    public Boolean isDecoy() {
        return isDecoy;
    }
}
