package io.github.bigbio.pgatk.spectra.ws.utils;

import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

public class Constants {

    public static final String SPECTRA_INDEX_NAME = "spectra-documents";
    public static final String USI_KEYWORD = "usi.keyword";
    public static final int MAX_PAGINATION_SIZE = 100;
    public static final IndexCoordinates INDEX_COORDINATES = IndexCoordinates.of(Constants.SPECTRA_INDEX_NAME);
    public static final String GENE_ACCESSIONS_KEYWORD = "geneAccessions.keyword";
    public static final String PROTEIN_ACCESSIONS_KEYWORD = "proteinAccessions.keyword";
    public static final String PEPTIDE_SEQUENCE = "pepSequence";
    public static final String PTM_MODIFICATION_POSITION_MAP_KEY = "modifications.positionMap.key";
    public static final String PTM_MODIFICATION_NAME = "modifications.modification.name";
    public static final String PTM_MODIFICATION_ACCESSION = "modifications.modification.accession";
    public static final String PTM_MODIFICATION_VALUE = "modifications.modification.value";
}
