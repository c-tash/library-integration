package ru.umeta.libraryintegration.service

import com.sun.deploy.util.OrderedHashSet
import gnu.trove.set.hash.TLongHashSet
import org.springframework.util.StringUtils
import ru.umeta.libraryintegration.inmemory.EnrichedDocumentRepository
import ru.umeta.libraryintegration.json.ModsParseResult
import ru.umeta.libraryintegration.json.ParseResult
import ru.umeta.libraryintegration.json.UploadResult
import ru.umeta.libraryintegration.model.EnrichedDocument
import ru.umeta.libraryintegration.model.EnrichedDocumentLite
import java.util.*


/**
 * Service for operating with [Document] and [EnrichedDocument]
 * Created by ctash on 28.04.2015.
 */
object DocumentService : AutoCloseable {

    private val DEFAULT_PROTOCOL = "Z39.50"
    private val DUPLICATE_SIZE = 1000

    val enrichedDocumentRepository = EnrichedDocumentRepository
    val stringHashService = StringHashService

    fun processDocumentList(resultList: List<ParseResult>, protocolName: String?): UploadResult {
        var newEnriched = 0
        var parsedDocs = 0

        for (parseResult in resultList) {
            if (parseResult is ModsParseResult) {
                try {
                    var author = parseResult.author
                    if (author.length > 255) {
                        author = author.substring(0, 255)
                    }
                    var title = parseResult.title
                    if (title.length > 255) {
                        title = title.substring(0, 255)
                    }

                    val authorId = stringHashService.getFromRepository(author)
                    val titleId = stringHashService.getFromRepository(title)
                    var isbn: String? = parseResult.isbn
                    if (isbn.isNullOrEmpty()) {
                    }
                    isbn = null
                    val enrichedDocument = EnrichedDocument(-1, authorId, titleId, isbn, null, Date(),
                            parseResult.publishYear)
                    enrichedDocumentRepository.save(enrichedDocument)
                    newEnriched++;
                    parsedDocs++
                } catch (e: Exception) {
                    throw e
                }


            }
        }
        return UploadResult(parsedDocs, newEnriched)
    }

    fun findEnrichedDocuments(document: EnrichedDocumentLite): List<EnrichedDocumentLite> {

        val dfs = DFS()
        dfs.apply(document);

        return dfs.component;
    }

    class DFS {

        val used = TLongHashSet()
        val component = ArrayList<EnrichedDocumentLite>()
        var filtered = HashSet<EnrichedDocumentLite>()
        var stack = Stack<EnrichedDocumentLite>()

        fun apply(document: EnrichedDocumentLite) {
            stack.add(document)
            while (!stack.isEmpty()) {
                val cur = stack.pop()
                val id = cur.id
                if (!used.contains(id)) {
                    val authorId = cur.authorId
                    val titleId = cur.titleId
                    val authorTokens = stringHashService.getById(authorId).tokens
                    val titleTokens = stringHashService.getById(titleId).tokens
                    used.add(id)
                    component.add(cur)
                    //filter documents which have the nearest measure of 0.7 or more
                    val current = filtered;
                    val nearDuplicates = enrichedDocumentRepository.getNearDuplicates(cur, current)


                    filtered = HashSet(nearDuplicates.filter {
                        if (used.contains(it.id)) {
                            false
                        } else {
                            val itAuthorTokens = stringHashService.getById(it.authorId).tokens
                            var authorTokensRatio: Double = authorTokens.size() * 1.0 / itAuthorTokens.size()
                            if (authorTokensRatio > 1) {
                                authorTokensRatio = 1.0 / authorTokensRatio
                            }

                            if (authorTokensRatio < 0.4) {
                                false
                            } else {
                                val itTitleTokens = stringHashService.getById(it.titleId).tokens
                                var titleTokensRatio: Double = titleTokens.size() * 1.0 / itTitleTokens.size()
                                if (titleTokensRatio > 1) {
                                    titleTokensRatio = 1.0 / titleTokensRatio
                                }

                                if (authorTokensRatio + titleTokensRatio < 0.7 * 2) {
                                    false
                                } else {
                                    (stringHashService.distance(authorTokens, itAuthorTokens) + stringHashService.distance
                                    (titleTokens, itTitleTokens) >= 0.7 * 2)
                                }
                            }
                        }
                    })

                    current.forEach { filtered.add(it) }
                    for (duplicate in filtered) {
                        stack.add(duplicate)
                    }
                }
            }
        }

    }


    fun addNoise(parseResult: ParseResult, saltLevel: Int): List<ParseResult>? {
        val author = parseResult.author
        val title = parseResult.title

        val authorLength = author.length
        val titleLength = title.length

        parseResult.isbn = null
        if (StringUtils.isEmpty(author) || StringUtils.isEmpty(title)) {
            return null
        } else {
            val resultList = ArrayList<ParseResult>()
            for (i in 0..DUPLICATE_SIZE - 1) {
                val newParseResult = parseResult.clone()
                val newAuthor = StringBuilder(author)
                val newTitle = StringBuilder(title)
                for (j in 0..saltLevel - 1) {
                    val rnd = Random()
                    var noiseIndex = rnd.nextInt(authorLength)
                    newAuthor.setCharAt(noiseIndex, '#')

                    noiseIndex = Random().nextInt(titleLength)
                    newTitle.setCharAt(noiseIndex, '#')
                }
                newParseResult.author = newAuthor.toString()
                newParseResult.title = newTitle.toString()
                resultList.add(newParseResult)
            }
            return resultList
        }
    }

    override fun close() {
        enrichedDocumentRepository.close()
        stringHashService.close()
    }

    fun getDocuments(): List<EnrichedDocumentLite> {
        return enrichedDocumentRepository.list
    }

    fun processDocumentListInit(resultList: List<ParseResult>, nothing: Nothing?): Any {
        var newEnriched = 0
        var parsedDocs = 0

        for (parseResult in resultList) {
            if (parseResult is ModsParseResult) {
                try {
                    var author = parseResult.author
                    if (author.length > 255) {
                        author = author.substring(0, 255)
                    }
                    var title = parseResult.title
                    if (title.length > 255) {
                        title = title.substring(0, 255)
                    }

                    val authorId = stringHashService.getFromRepositoryInit(author)
                    val titleId = stringHashService.getFromRepositoryInit(title)
                    var isbn: String? = parseResult.isbn
                    if (isbn.isNullOrEmpty()) {
                    }
                    isbn = null
                    val enrichedDocument = EnrichedDocument(-1, authorId, titleId, isbn, null, Date(),
                            parseResult.publishYear)
                    enrichedDocumentRepository.saveInit(enrichedDocument)
                    newEnriched++;
                    parsedDocs++
                } catch (e: Exception) {
                    throw e
                }


            }
        }
        return UploadResult(parsedDocs, newEnriched)
    }
}
