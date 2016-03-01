package ru.umeta.libraryintegration.inmemory

import gnu.trove.map.hash.TIntLongHashMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.set.hash.TIntHashSet
import ru.umeta.libraryintegration.fs.StringHashFsPersister
import ru.umeta.libraryintegration.model.StringHash
import ru.umeta.libraryintegration.service.getTokens
import java.util.*

/**
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 12.11.2015.
 */
object StringHashRepository : AutoCloseable {

    private val mapHashCodeToId = TIntLongHashMap()
    private val mapIdToSimHash = TLongIntHashMap()
    private val mapIdToTokens = HashMap<Long, TIntHashSet>()

    private var identity: Long = 0
    private val stringHashFsPersister = StringHashFsPersister
    public var isInit = false;

    init {
        val lastId = stringHashFsPersister.fillMaps(mapHashCodeToId, mapIdToSimHash, mapIdToTokens)
        identity = lastId + 1
        isInit = true;
        //        fsPersister.fillMap(tokens);
    }

    fun getByHashCode(string: String): Long {
        return mapHashCodeToId[string.javaHashCode()]
    }

    fun getSimHashById(id: Long): Int {
        return mapIdToSimHash[id]
    }

    fun save(stringHash: StringHash, value: String) {
        val id = identity++
        stringHash.id = id
        mapHashCodeToId.put(value.javaHashCode(), id)
        mapIdToSimHash.put(id, stringHash.simHash)
        mapIdToTokens.put(id, getTokens(value))
        stringHashFsPersister.save(stringHash, value)
    }

    fun getStringHashById(id: Long): StringHash {
        val simHash = mapIdToSimHash[id]
        val tokens = mapIdToTokens[id] ?: throw RuntimeException("token set for simHash with id=\"$id\" is null.")
        return StringHash(id, tokens, simHash)
    }

    override fun close() {
        stringHashFsPersister.close()
    }

    fun saveInit(stringHash: StringHash, value: String) {
        val id = identity++
        stringHash.id = id
        mapHashCodeToId.put(value.javaHashCode(), id)
        stringHashFsPersister.save(stringHash, value)
    }
}

fun String.javaHashCode(): Int {
    return (this as java.lang.String).hashCode();
}
