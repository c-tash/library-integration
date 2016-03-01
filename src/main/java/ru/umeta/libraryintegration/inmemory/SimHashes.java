package ru.umeta.libraryintegration.inmemory;

import ru.umeta.libraryintegration.model.StringHash;

/**
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 28.02.2016.
 */
public class SimHashes {
    int hashes[][][][] = new int[4][4][4][4];

    public SimHashes(StringHash title, StringHash author) {
        byte[] aBytes = new byte[4];
        byte[] tBytes = new byte[4];

        aBytes[0] = author.hashPart1();
        aBytes[1] = author.hashPart3();
        aBytes[2] = author.hashPart2();
        aBytes[3] = author.hashPart4();

        tBytes[0] = title.hashPart1();
        tBytes[1] = title.hashPart2();
        tBytes[2] = title.hashPart3();
        tBytes[3] = title.hashPart4();

        for (int ti = 1; ti <= 3; ti++) {
            for (int tj = ti+1; tj <= 4; tj++) {
                for (int ai = 1; ai <=3; ai++) {
                    for (int aj = ai+1; aj <=4; aj++) {
                        hashes[ti-1][tj-1][ai-1][aj-1] = getHashWithoutYear(
                                tBytes[ti-1],
                                tBytes[tj-1],
                                aBytes[ai-1],
                                aBytes[aj-1]);
                    }
                }
            }
        }
    }

    public int getByIndex(int ti, int tj, int ai, int aj) {
        ti--;
        tj--;
        ai--;
        aj--;
        return hashes[ti][tj][ai][aj];
    }

    private int getHashWithoutYear(byte hash1, byte hash2, byte hash3, byte hash4) {
        //shift is of the size of a byte
        int shift = 8;
        int result = (int) hash1;
        result = (result << shift) + (int) hash2;
        result = (result << shift) + (int) hash3;
        result = (result << shift) + (int) hash4;
        return result;
    }
}
