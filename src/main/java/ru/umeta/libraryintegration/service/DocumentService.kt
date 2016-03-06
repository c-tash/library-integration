package ru.umeta.libraryintegration.service

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

    fun findEnrichedDocuments(cur: EnrichedDocumentLite, used: TLongHashSet): DFS {
        val authorId = cur.authorId
        val titleId = cur.titleId
        val authorTokens = stringHashService.getById(authorId).tokens
        val titleTokens = stringHashService.getById(titleId).tokens
        used.add(cur.id)
        //filter documents which have the nearest measure of 0.7 or more
        var nearDuplicates = enrichedDocumentRepository.getNearDuplicates(cur, used)
        var iterationsLength = 0L
        var iterationsSetInter = 0L
        nearDuplicates = nearDuplicates.filter {
            if (used.contains(it.id)) {
                false
            } else {
                val itAuthorTokens = stringHashService.getById(it.authorId).tokens
                var authorTokensRatio: Double = authorTokens.size() * 1.0 / itAuthorTokens.size()
                if (authorTokensRatio > 1) {
                    authorTokensRatio = 1.0 / authorTokensRatio
                }

                if (authorTokensRatio < 0.4) {
                    iterationsLength++
                    false
                } else {
                    val itTitleTokens = stringHashService.getById(it.titleId).tokens
                    var titleTokensRatio: Double = titleTokens.size() * 1.0 / itTitleTokens.size()
                    if (titleTokensRatio > 1) {
                        titleTokensRatio = 1.0 / titleTokensRatio
                    }

                    if (authorTokensRatio + titleTokensRatio < 0.7 * 2) {
                        iterationsLength++
                        false
                    } else {
                        iterationsSetInter++
                        (stringHashService.distance(authorTokens, itAuthorTokens) + stringHashService.distance
                        (titleTokens, itTitleTokens) >= 0.7 * 2)
                    }
                }
            }
        }

        val dfs = DFS(nearDuplicates, iterationsLength, iterationsSetInter)
        return dfs
    }

    data class DFS(
        val component: List<EnrichedDocumentLite>,
        var iterationsLength: Long,
        var iterationsSetInter: Long)



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
        return enrichedDocumentRepository.getList();
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
