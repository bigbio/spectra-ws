package io.github.bigbio.pgatk.spectra.ws.utils;


import io.github.bigbio.pgatk.io.utils.Tuple;

public class WsUtils {

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size) {
        if (size > Constants.MAX_PAGINATION_SIZE || size < 0)
            size = Constants.MAX_PAGINATION_SIZE;
        if (start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size, int maxPageSize) {
        if (size > maxPageSize || size < 0)
            size = maxPageSize;
        if (start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static long validatePage(int page, long totalPages) {
        if (page < 0)
            return 0;
        if (page > totalPages)
            return totalPages;
        return page;
    }

    public static Tuple<Boolean, String> validatePeptideSeqRegex(String pepSeq) {
        if (pepSeq == null) {
            throw new IllegalArgumentException("PeptideSequenceRegex can't be null");
        }
        String s = pepSeq.replaceAll("[^A-Z]", "");
        if (s.trim().length() < 4) {
            throw new IllegalArgumentException("PeptideSequence should contain at least 4 valid characters");
        }
        return new Tuple<>(true, "");
    }
}
