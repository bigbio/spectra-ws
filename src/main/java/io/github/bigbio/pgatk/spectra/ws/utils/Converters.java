package io.github.bigbio.pgatk.spectra.ws.utils;

import io.github.bigbio.pgatk.io.pride.ArchiveSpectrum;
import io.github.bigbio.pgatk.spectra.ws.model.ElasticSpectrum;

public class Converters {

    public static ArchiveSpectrum elasticToArchiveSpectrum(ElasticSpectrum es) {
        //TODO below fileds can't be mapped to ArchiveSpectrum
//        private List<String> proteinAccessions;  ???
//        private double precursorCharge;   ?? toINt??
//        private List<String> projectAssays;  ?? projectAccessions??
//        private List<String> pxProjects; ?? projectAccessions??
//        private List<CvParam> species; ??
//        private List<String> modificationNames; ??
//        private List<String> modificationAccessions; ??
//        private List<String> text; ??

        return ArchiveSpectrum.builder()
                .usi(es.getUsi())
                .peptideSequence(es.getPepSequence())
                .precursorMz(es.getPrecursorMz())
                .precursorCharge((int) Math.round(es.getPrecursorCharge())) //TODO : is this OK?? double to int?
//                .modifications(es.getModifications())
                .masses(es.getMasses().toArray(new Double[0]))
                .intensities(es.getIntensities().toArray(new Double[0]))
                .retentionTime(es.getRetentionTime())
                .properties(es.getProperties())
                .missedCleavages(es.getMissedCleavages())
                .annotations(es.getAnnotations())
                .qualityEstimationMethods(es.getQualityEstimationMethods())
                .build();
    }
}
