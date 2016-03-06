package ru.umeta.libraryintegration.fs

import gnu.trove.map.hash.TIntLongHashMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.set.hash.TIntHashSet
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator
import org.apache.commons.io.output.FileWriterWithEncoding
import org.springframework.util.StringUtils
import ru.umeta.libraryintegration.inmemory.javaHashCode
import ru.umeta.libraryintegration.model.StringHash
import ru.umeta.libraryintegration.service.getTokens
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by k.kosolapov on 12/2/2015.
 */
object StringHashFsPersister : AutoCloseable {

    const val SEPARATOR = "|"
    const val UTF_8 = "UTF-8"

    private val executorService = Executors.newSingleThreadExecutor()

    private val storageFile = File("stringHash.blob")

    private val writerWithEncoding: FileWriterWithEncoding

    init {
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        writerWithEncoding = FileWriterWithEncoding(storageFile, Charset.forName(UTF_8), true)
    }

    fun save(stringHash: StringHash, value: String) {
        executorService.execute {
            try {
                writerWithEncoding.write(Hex.encodeHexString(
                        byteArrayOf(stringHash.hashPart1(),
                                stringHash.hashPart2(),
                                stringHash.hashPart3(),
                                stringHash.hashPart4())))
                writerWithEncoding.write(SEPARATOR)
                writerWithEncoding.write(stringHash.id.toString())
                writerWithEncoding.write(SEPARATOR)
                writerWithEncoding.write(value)
                writerWithEncoding.write(SEPARATOR + "\n")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun fillMaps(mapHashCodeToId: TIntLongHashMap, mapIdToSimHash: TLongIntHashMap,
                 mapIdToTokens: HashMap<Long, TIntHashSet>): Long {
        var lastId: Long = 0
        try {
            val it = FileUtils.lineIterator(storageFile, UTF_8)
            try {
                while (it.hasNext()) {
                    try {

                        val line = it.nextLine()
                        var splitStrings = StringUtils.tokenizeToStringArray(line, SEPARATOR)

                        if (splitStrings.size != 3) {
                            if (splitStrings.size == 2 && "00000000" == splitStrings[0]) {
                                val newSplitStrings = arrayOfNulls<String>(3)
                                newSplitStrings[0] = splitStrings[0]
                                newSplitStrings[1] = splitStrings[1]
                                newSplitStrings[2] = ""
                                splitStrings = newSplitStrings
                            } else {
                                continue
                            }
                        }

                        val bytes = Hex.decodeHex(splitStrings[0].toCharArray())
                        val hashPart1: Byte
                        val hashPart2: Byte
                        val hashPart3: Byte
                        val hashPart4: Byte

                        if (bytes.size < 4) {
                            continue
                        } else {
                            hashPart1 = bytes[0]
                            hashPart2 = bytes[1]
                            hashPart3 = bytes[2]
                            hashPart4 = bytes[3]
                        }
                        val id = splitStrings[1].toLong()
                        lastId = Math.max(id, lastId)
                        val value = splitStrings[2]
                        val tokens = getTokens(value)
                        val simHash = StringHash.Util.collectParts(hashPart1, hashPart2, hashPart3, hashPart4)

                        mapHashCodeToId.put(value.javaHashCode(), id)
                        mapIdToSimHash.put(id, simHash)
                        mapIdToTokens.put(id, tokens)

                    } catch (e: DecoderException) {
                        e.printStackTrace()
                    }

                }

            } finally {
                LineIterator.closeQuietly(it)
            }
        } catch (e: IOException) {
            println("Cannot open the storage file")
        }

        return lastId
    }

    override fun close() {
        writerWithEncoding.close()
    }
}
