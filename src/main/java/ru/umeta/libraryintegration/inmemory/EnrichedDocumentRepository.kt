package ru.umeta.libraryintegration.inmemory

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import gnu.trove.set.hash.TLongHashSet
import ru.umeta.libraryintegration.fs.EnrichedDocumentFsPersister
import ru.umeta.libraryintegration.model.Document
import ru.umeta.libraryintegration.model.EnrichedDocument
import ru.umeta.libraryintegration.model.EnrichedDocumentLite
import ru.umeta.libraryintegration.model.StringHash
import ru.umeta.libraryintegration.service.StringHashService
import java.util.*

/**
 * The repository consists of large amount of hashmaps to get fast access to near duplicates.
 * The maps have the following structure:
 * year -> t1 -> t2 -> a1
 * ||    \\ -> a2
 * \\ -> t3 -> a1
 * ||    \\ -> a2
 * \\ -> t4 -> a1
 * \\ -> a2
 * ...
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 12.11.2015.
 */
object EnrichedDocumentRepository : IEnrichedDocumentRepository, AutoCloseable {

    override fun getNearDuplicates(document: EnrichedDocumentLite): List<EnrichedDocumentLite> {
        return getNearDuplicates(document, TLongHashSet())
    }

    override fun getNearDuplicates(document: Document): MutableList<EnrichedDocumentLite> {
        throw UnsupportedOperationException()
    }

    private val BATCH_SIZE = 10000

    internal val fsPersister = EnrichedDocumentFsPersister;
    internal val stringHashService = StringHashService;
    internal val list = ArrayList<EnrichedDocumentLite>()
    //no year maps
    internal var maps = SimHashMaps()


    private var identity = 0L

    init {
        while (StringHashRepository.isInit == false) {
            (this as java.lang.Object).wait(10)
        }
        val lastId = fsPersister.applyToPersisted { enrichedDocument: EnrichedDocument -> this.putIntoMaps(enrichedDocument) }
        identity = lastId + 1
    }


    override fun getNearDuplicates(document: EnrichedDocumentLite, current: TLongHashSet)
            : List<EnrichedDocumentLite> {
        val authorId = document.authorId
        val titleId = document.titleId

        val author = stringHashService.getById(authorId)
        val title = stringHashService.getById(titleId)

        val hashes = SimHashes(title, author);

        val result = HashSet<EnrichedDocumentLite>()

        val filterFunction = {it: EnrichedDocumentLite ->
            if (!current.contains(it.id)) {
                result.add(it)
            }
        }

        for (ti in 1..3) {
            for (tj in ti+1..4) {
                for (ai in 1..3) {
                    for (aj in ai+1..4) {
                        val hash = hashes.getByIndex(ti, tj, ai, aj)
                        maps.getByIndex(ti, tj, ai, aj).get(hash).forEach(filterFunction)
                    }
                }
            }
        }

        return result.toList()
    }

    private fun getHashWithoutYear(hash1: Byte, hash2: Byte, hash3: Byte, hash4: Byte): Int {
        //shift is of the size of a byte
        val shift = 8
        var result = hash1.toInt()
        result = (result shl shift) + hash2.toInt()
        result = (result shl shift) + hash3.toInt()
        result = (result shl shift) + hash4.toInt()
        return result
    }

    override fun getNearDuplicatesWithIsbn(document: Document): List<EnrichedDocumentLite> {
        return emptyList()
    }

    override fun getNearDuplicatesWithNullIsbn(document: Document): List<EnrichedDocumentLite> {
        return emptyList()
    }

    override fun getNearDuplicatesWithPublishYear(document: Document): List<EnrichedDocumentLite> {
        return emptyList()
    }

    private fun getHashWithYear(year: Int, hash1: Byte, hash2: Byte, hash3: Byte): Int {
        val shift = 8
        var result = year
        val mask = 255
        result = ((result shl shift) + (hash1.toInt() and mask))
        result = (result shl shift) + (hash2.toInt() and mask)
        result = (result shl shift) + (hash3.toInt() and mask)
        return result
    }

    override fun save(enrichedDocument: EnrichedDocument) {
        enrichedDocument.id = identity++
        putIntoMaps(enrichedDocument)
        fsPersister.save(enrichedDocument)

    }

    private fun putIntoMaps(enrichedDocument: EnrichedDocument) {
        val authorHash: StringHash
        val titleHash: StringHash
        val year: Int?
        val lite: EnrichedDocumentLite
        try {
            val id = enrichedDocument.id
            val isbn = enrichedDocument.isbn
            val author = enrichedDocument.author
            val title = enrichedDocument.title
            lite = EnrichedDocumentLite(id, author, title)
            if (isbn != null) {
                lite.nullIsbn = false
                //isbnMap.put(isbn.hashCode(), lite)
            }

            authorHash = stringHashService.getById(author)
            titleHash = stringHashService.getById(title)
            year = enrichedDocument.publishYear
        } catch (e: RuntimeException) {
            println(e.message)
            return
        }

        val hashes = SimHashes(titleHash, authorHash);

        for (ti in 1..3) {
            for (tj in ti+1..4) {
                for (ai in 1..3) {
                    for (aj in ai+1..4) {
                        val hash = hashes.getByIndex(ti, tj, ai, aj)
                        maps.getByIndex(ti, tj, ai, aj).put(hash, lite)
                    }
                }
            }
        }

        list.add(lite)
    }

    override fun update(enrichedDocument: EnrichedDocument) {
        //TODO
    }

    override fun getById(id: Long): EnrichedDocument? {
        //TODO
        return null
    }

    override fun close() {
        fsPersister.close()
    }

    fun saveInit(enrichedDocument: EnrichedDocument) {
        enrichedDocument.id = identity++
        //putIntoMaps(enrichedDocument)
        fsPersister.save(enrichedDocument)
    }

    fun getList(): List<EnrichedDocumentLite> {
        return list;
    }

}
