package io.github.bigbio.pgatk.spectra.ws.model;
import static io.github.bigbio.pgatk.spectra.ws.utils.Constants.*;


public enum PtmKey {

    name(PTM_MODIFICATION_NAME),
    accession(PTM_MODIFICATION_ACCESSION),
    mass(PTM_MODIFICATION_VALUE);

    private String elastname;

    PtmKey(String name) {
        this.elastname = name;
    }

    public String getElastname() {
        return elastname;
    }
}
