package ru.umeta.libraryintegration.service

import com.google.common.base.Strings
import gnu.trove.TCollections
import gnu.trove.map.hash.TIntIntHashMap
import gnu.trove.set.hash.TIntHashSet
import org.springframework.beans.factory.annotation.Autowired
import ru.umeta.libraryintegration.inmemory.StringHashRepository
import ru.umeta.libraryintegration.model.StringHash
import ru.umeta.libraryintegration.model.bigrammToInt
import ru.umeta.libraryintegration.model.getBigrammChars
import ru.umeta.libraryintegration.util.MD5To32Algorithm
import java.util.*

/**
 * Created by k.kosolapov on 14.05.2015.
 */
class StringHashService
@Autowired
constructor(private val stringHashRepository: StringHashRepository) {

    private val tokenMap = TIntIntHashMap();

    fun getStringHash(string: String): StringHash {
        var simHash = 0
        val tokens: TIntHashSet
        if (string.length < 2) {
            simHash = 0
            tokens = TIntHashSet(0)
        } else {
            tokens = getSimHashTokens(string)
            /**
             * 32 is hash size
             */
            val preHash = IntArray(32)
            Arrays.fill(preHash, 0)

            for (token in tokens) {
                var tokenHash: Int
                var mapTokenHash: Int? = tokenMap[token]
                if (mapTokenHash == null) {
                    tokenHash = MD5To32Algorithm.getHash(token)
                    tokenMap.put(token, tokenHash)
                } else {
                    tokenHash = mapTokenHash
                }
                for (i in 0..31) {
                    if (tokenHash % 2 != 0) {
                        preHash[i]++
                    } else {
                        preHash[i]--
                    }
                    tokenHash = tokenHash.ushr(1)
                }

            }

            for (i in 0..31) {
                if (preHash[i] >= 0) {
                    simHash++
                }
                simHash *= 2
            }
        }


        return StringHash(-1, tokens, simHash)
    }

    fun getSimHashTokens(string: String): TIntHashSet {
        return getTokens(string)
    }

    fun getFromRepository(string: String): StringHash {
        var string = string
        if (string.length > 255) {
            string = string.substring(0, 255)
        }

        var stringHashId = stringHashRepository.getByHashCode(string)
        if (stringHashId == TROVE_NO_VALUE) {
            val stringHash = getStringHash(string)
            stringHashRepository.save(stringHash, string)
            return stringHash
        } else {
            return stringHashRepository.getStringHashById(stringHashId)
        }
    }

    fun getById(id: Long): StringHash {
        return stringHashRepository.getStringHashById(id);
    }

    fun distance(tokens1: TIntHashSet, tokens2: TIntHashSet): Double {
        val union = TIntHashSet(tokens1)
        val intersection = TIntHashSet(tokens1)

        union.addAll(tokens2)
        intersection.retainAll(tokens2)

        return (intersection.size() * 1.0) / (union.size() * 1.0)
    }

    fun distance(stringId: Long, otherStringId: Long): Double {
        val stringHash = getById(stringId)
        val otherStringHash = getById(otherStringId)
        return distance(stringHash.tokens, otherStringHash.tokens)
    }

    fun distance(stringHash: StringHash, otherStringId: Long): Double {
        val otherStringHash = getById(otherStringId)
        return distance(stringHash.tokens, otherStringHash.tokens)
    }

    companion object {
        public val TROVE_NO_VALUE = 0L
    }
}

public fun getTokens(string: String): TIntHashSet {
    if (Strings.isNullOrEmpty(string)) {
        return TIntHashSet()
    }

    val tokens = TIntHashSet()
    for (i in 0..string.length - 1 - 1) {
        val bigramm = bigrammToInt(string.substring(i, i + 2));
        if (!tokens.contains(bigramm)) {
            tokens.add(bigramm)
        }
    }
    return tokens
}
