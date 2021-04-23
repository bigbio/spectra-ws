package io.github.bigbio.pgatk.spectra.ws.utils;

import io.github.bigbio.pgatk.elastic.multiomics.model.ElasticSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.Spectrum;

public class Converters {

    public static Spectrum elasticToArchiveSpectrum(ElasticSpectrum es) {
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
                .geneLocalizations(es.getGeneLocalizations())
                .geneAccessions(es.getGeneAccessions())
                .proteinLocalizations(es.getProteinLocalizations())
                .proteinAccessions(es.getProteinAccessions())
                .sample(es.getSample())
                .organism(es.getOrganism())
                .qualityScores(es.getQualityScores())
                .msAnnotations(es.getMsAnnotations())
                .biologicalAnnotations(es.getBiologicalAnnotations())
                .build();

        return spectrum;
    }
}
