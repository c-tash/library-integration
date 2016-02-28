package ru.umeta.libraryintegration.inmemory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ru.umeta.libraryintegration.model.EnrichedDocument;
import ru.umeta.libraryintegration.model.EnrichedDocumentLite;

/**
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 28.02.2016.
 */
public class SimHashMaps {

    ArrayListMultimap[][][][] maps = new ArrayListMultimap[4][4][4][4];

    public ArrayListMultimap<Integer, EnrichedDocumentLite> getByIndex(int ti, int tj, int ai, int aj) {
        ti--;
        tj--;
        ai--;
        aj--;
        ArrayListMultimap<Integer, EnrichedDocumentLite> result = maps[ti][tj][ai][aj];
        if (result == null) {
            result = ArrayListMultimap.<Integer, EnrichedDocumentLite>create();
            maps[ti][tj][ai][aj] = result;
        }
        return result;
    }

}
