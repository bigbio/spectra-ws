package io.github.bigbio.pgatk.spectra.ws.utils;

import io.github.bigbio.pgatk.elastic.multiomics.model.ElasticSpectrum;
import io.github.bigbio.pgatk.io.pride.CvParam;
import io.github.bigbio.pgatk.io.utils.Tuple;
import io.github.bigbio.pgatk.spectra.ws.model.PtmKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FilterGetByPtmSpectrum implements Function<List<ElasticSpectrum>, List<ElasticSpectrum>> {

    final PtmKey ptmKey;
    final String ptmValue;
    final List<Integer> positions;

    public FilterGetByPtmSpectrum(PtmKey ptmKey, String ptmValue, List<Integer> positions) {
        this.ptmKey = ptmKey;
        this.ptmValue = ptmValue;
        this.positions = positions;
    }

    @Override
    public List<ElasticSpectrum> apply(List<ElasticSpectrum> elasticSpectrums) {
        String ptmKeyName = ptmKey.name();
        List<ElasticSpectrum> elasticSpectrumsFiltered = new ArrayList<>();
        elasticSpectrums.forEach(a -> {
            a.getModifications().forEach(m -> {
                if (positions.contains(m.getPosition())) {
                    if ((ptmKeyName.equals("accession") && m.getAccession().equals(ptmValue)) ||
                            (ptmKeyName.equals("name") && m.getModification().equals(ptmValue))) {
                            elasticSpectrumsFiltered.add(a);
                            return;
                        }
                }
            });
        });
        return elasticSpectrumsFiltered;
    }
}
