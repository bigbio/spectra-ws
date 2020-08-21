:green_book: Full Documentation with code samples : https://spectra-ws.readthedocs.io/


This is a Restful API that provides a programatic interface to access to peptide evidences previously identified in mass spectrometry experiments.

:information_source: The API is published in the following URL: https://www.ebi.ac.uk/pride/multiomics/ws

Mass spectrum JSON structure
---------------------------------

The MSLookup service provides mass spectrometry evidences for peptides, with special focus on modified peptides (PTMs - posttranslational modifications) and single aminoacid variants. The structure of the spectra provided on each endpoint is the following:

```
   {
     "id": "NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80003",
     "usi": "NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80003",
     "pepSequence": "AQLGVQAFADALLIIPK",
     "proteinAccessions": [ "P40227-2", "ENSP00000275603.4"],
     "geneAccessions": [CCT6A","ENSG00000146731.11","ENST00000335503.3"],
     "precursorMz": 514.8157,
     "precursorCharge": 4,
     "projectAssays": null,
     "pxProjects": null,
     "species": [],
     "modifications": [
         {
           "neutralLoss": null,
           "positionMap": [
              {
                "key": 0,
                "value": []
              },
              {
                "key": 16,
                "value": []
              }
            ],
           "modification": {
              "cvLabel": "UNIMOD",
              "accession": "UNIMOD:214",
              "name": "iTRAQ4plex",
              "value": "144.102063"
           },
          "attributes": null
         }
     ],
     "masses": [145.1084,199.1806,458.2939],
     "intensities": [5123.7,6716.8,2049.7],
     "retentionTime": null,
     "properties": null,
     "missedCleavages": 0,
     "annotations": null,
     "qualityEstimationMethods": [],
     "text": null
   }
  ```

Main spectra attributes
-----------------------

The mass spectrum attributes can be divided in three main groups:

- biology properties:
    - Protein accessions in ENSEMBL and UNIPROT that contains the corresponding peptides; gene accessions which represent a list of gene names, gene and transcript accessions from ENSEMBL that contains the corresponding peptides.
    - Post-translational modifications: a list of post-translational modifications identified by mass spectrometry including position, monoisotopic mass, and UNIMOD accession if available.
    - Additional metadata: species, sample conditions, tissue, cell-line.

- mass spectrometry properties: Spectrum information including (precursor mz, charge and peak list), additional information such as retention time and missedclevages.

- statistical assessment: additional quality and statistical assessment scores such as search engine scores, p-values, q-values.

If the information is not available empty lists or `null` values are provided.
