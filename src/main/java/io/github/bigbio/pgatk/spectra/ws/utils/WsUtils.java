package io.github.bigbio.pgatk.spectra.ws.utils;


import io.github.bigbio.pgatk.io.utils.Triple;
import io.github.bigbio.pgatk.io.utils.Tuple;

public class WsUtils {

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size) {
        if(size > Constants.MAX_PAGINATION_SIZE || size < 0 )
            size = Constants.MAX_PAGINATION_SIZE;
        if(start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size, int maxPageSize) {
        if(size > maxPageSize || size < 0 )
            size = maxPageSize;
        if(start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static long validatePage(int page, long totalPages) {
        if(page < 0)
            return 0;
        if(page > totalPages)
            return totalPages;
        return page;
    }
}
