package io.github.bigbio.pgatk.spectra.ws.utils;

import io.github.bigbio.pgatk.elastic.multiomics.model.ElasticSpectrum;
import io.github.bigbio.pgatk.io.pride.AvroTuple;
import io.github.bigbio.pgatk.spectra.ws.model.Spectrum;

import java.util.HashSet;

public class Converters {

    public static Spectrum elasticToArchiveSpectrum(ElasticSpectrum es) {
        HashSet<AvroTuple> biologicalAnnotations = (es.getBiologicalAnnotations() != null)?new HashSet<>(es.getBiologicalAnnotations()): new HashSet<>();

        Spectrum spectrum = Spectrum.builder()
                .usi(es.getUsi())
                .pepSequence(es.getPepSequence())
                .peptidoform(es.getPeptidoform())
                .masses(es.getMasses())
                .intensities(es.getIntensities())
                .precursorCharge(es.getPrecursorCharge())
                .precursorMz(es.getPrecursorMz())
                .isDecoy(es.getIsDecoy())
                .missedCleavages(es.getMissedCleavages())
                .msLevel(es.getMsLevel())
                .retentionTime(es.getRetentionTime())
                .peptideIntensity(es.getPeptideIntensity())
                .modifications(es.getModifications())
                .geneLocalizations(new HashSet<>(es.getGeneLocalizations()))
                .geneAccessions(new HashSet<>(es.getGeneAccessions()))
                .proteinLocalizations(new HashSet<>(es.getProteinLocalizations()))
                .proteinAccessions(new HashSet<>(es.getProteinAccessions()))
                .sample(es.getSample())
                .organism(es.getOrganism())
                .qualityScores(new HashSet<>(es.getQualityScores()))
                .msAnnotations(new HashSet<>(es.getMsAnnotations()))
                .biologicalAnnotations(biologicalAnnotations)
                .build();

        return spectrum;
    }
}
