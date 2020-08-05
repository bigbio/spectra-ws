package io.github.bigbio.pgatk.spectra.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.bigbio.pgatk.io.pride.CvParam;
import io.github.bigbio.pgatk.io.pride.IdentifiedModification;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.*;

import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.SPECTRA_INDEX_NAME;

@Data
@Builder
@Document(indexName = SPECTRA_INDEX_NAME,  shards = 4)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticSpectrum {

    @Id
    private String id;

    @Field(name = "usi", store = true, type = FieldType.Text)
    private String usi;

    @Field( name = "pepSequence", store = true, type = FieldType.Text)
    private String pepSequence;

    @Field( name = "proteinAccessions", store = true)
    private List<String> proteinAccessions;

    @Field( name = "precursorMz", store = true)
    private double precursorMz;

    @Field( name = "precursorCharge", store = true)
    private double precursorCharge;

    @Field( name = "projectAssaysList", store = true)
    private List<String> projectAssays;

    @Field( name = "pxProjects", store = true)
    private List<String> pxProjects;

    @Field( name = "species", store = true, type = FieldType.Nested, includeInParent = true)
    private List<CvParam> species;

    @Field( name = "modifications", store = true, type = FieldType.Nested, includeInParent = true)
    private List<IdentifiedModification> modifications;

    @Field( name = "modificationNames", store = true)
    private List<String> modificationNames;

    @Field( name = "modificationAccessions", store = true)
    private List<String> modificationAccessions;

    @Field( name = "masses", store = true, index = false)
    private List<Double> masses;

    @Field( name = "intensities", store = true, index = false)
    private List<Double> intensities;

    @Field( name = "retentionTime", store = true)
    Double retentionTime;

    @Field( name = "properties", store = true, type = FieldType.Nested, includeInParent = true)
    Set<CvParam> properties;

    @Field( name = "missedCleavages", store = true)
    Integer missedCleavages;

    @Field(name = "annotations", store = true)
    List<String> annotations;

    @Field(name = "qualityEstimationMethods", store = true, type = FieldType.Nested, includeInParent = true)
    private Set<CvParam> qualityEstimationMethods;

    @Field( name = "text", store = true)
    private List<String> text;


}
