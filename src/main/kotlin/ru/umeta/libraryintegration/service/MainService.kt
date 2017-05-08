package ru.umeta.libraryintegration.service

import org.apache.commons.io.output.FileWriterWithEncoding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.umeta.libraryintegration.inmemory.InMemoryRepository
import ru.umeta.libraryintegration.json.UploadResult
import ru.umeta.libraryintegration.parser.IXMLParser
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import javax.annotation.PostConstruct

/**
 * The main service to handle the integration logic
 * Created by k.kosolapov on 14/04/2015.
 */
@Component
open class MainService
@Autowired
constructor(val parser: IXMLParser,
            val documentService: DocumentService,
            val stringHashService: StringHashService,
            val repository: InMemoryRepository) {

    companion object {
        val logger = Logger.getLogger(MainService::class.java.name)
    }

    @PostConstruct
    fun addFileLogger() {
        val fileHandler = FileHandler("library.log", true)
        fileHandler.formatter = SimpleFormatter()
        logger.addHandler(fileHandler)
    }

    @Throws(InterruptedException::class)
    fun parseDirectory(path: String): UploadResult {
        logger.info("Start parsing directory.")
        val fileList = getFilesToParse(path)
        var total = 0
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val size = resultList.size
            total += size
            logger.info("resultList size is " + size)
            val uploadResult = documentService.processDocumentList(resultList, null, true)
            val endTime = System.nanoTime()
            logger.info("The documents bulk is added in " + (endTime - startTime).toDouble() / 1000000000.0 + ". Total: " + total)
            result.parsedDocs = result.parsedDocs + uploadResult.parsedDocs
            result.newEnriched = result.newEnriched + uploadResult.newEnriched
        }

        return result
    }

    fun find() {
        repository.fillMapsFromRedis()
        logger.info("Start finding duplicates.")
        (FileWriterWithEncoding(File("duplicates.blob"), Charset.forName("UTF-8"), false).use {
            writer ->
            var i = 1
            var iterationsIsbn = 0L
            var iterationsYear = 0L
            var iterationsLeft = 0L
            val startId = 1
            var id = startId
            val maxId = repository.getDocCount()
            while (id <= maxId) {
                val doc = documentService.getDoc(id)
                if (doc != null) {
                    writer.write("SE $i\n")
                    writer.write("$id\n")
                    i++
                    val dfsResult = documentService.findEnrichedDocuments(doc)
                    iterationsIsbn += dfsResult.iterationsIsbn
                    iterationsYear += dfsResult.iterationsYear
                    iterationsLeft += dfsResult.remainingDocs
                    dfsResult.component.forEach { (id) ->
                        writer.write("$id\n")
                    }
                    if (id % 100000 == 0) {
                        logger.info(i.toString())
                        logger.info("Average Iterations on Isbn ${iterationsIsbn * 1.0 / (id - startId)}")
                        logger.info("Average Iterations on Publish Year ${iterationsYear * 1.0 / (id - startId)}")
                        logger.info("Average Iterations Left ${iterationsLeft * 1.0 / (id - startId)}")
                        logger.info("Marked ${(id - startId)}")
                        logger.info("----------------------------------------------------------------------")
                    }
                } else {
                    logger.info("Last document id is $id")
                    break
                }
                id++
            }
            logger.info("Average Iterations on Publish Year ${iterationsYear * 1.0 / (id - startId)}")
            logger.info("Average Iterations on Isbn ${iterationsIsbn * 1.0 / (id - startId)}")
            logger.info("Average Iterations Left ${iterationsLeft * 1.0 / (id - startId)}")
            logger.info("Marked ${(id - startId)}")
        })
    }

    fun parseDirectoryInit(path: String): Any {
        logger.info("Start parsing directory.")
        val fileList = getFilesToParse(path)
        var total = 0
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val size = resultList.size
            total += size
            logger.info("resultList size is " + size)
            documentService.processDocumentListInit(resultList)
            val endTime = System.nanoTime()
            logger.info("The documents bulk is added in " + (endTime - startTime).toDouble() / 1000000000.0 + ". Total: " + total)
        }

        return result
    }

    fun collect() {
        repository.fillMapsFromRedisWithTokens()
        JavaDuplicateService(repository, stringHashService).parse()
    }

    fun collectLegacy() {
        repository.fillMapsFromRedisWithTokens()
        JavaDuplicateService(repository, stringHashService).parserLegacy()
    }

    fun strings() {
        FileWriterWithEncoding("docs.blob", "UTF-8").use { writer ->
            repository.docStorage.forEach {
                if (it != null) {
                    writer.write("${it.id}\n")
                    writer.write("${it.authorToLong()}\n")
                    writer.write("${it.titleToLong()}\n")
                    writer.write("${it.isbnYearToLong()}\n")
                }
            }
        }
    }

    fun collectDebug() {
        repository.fillMapsFromRedisWithTokens()
        JavaDuplicateService(repository, stringHashService).parseDebug()
    }

    fun getStat() {
        val map = HashMap<Int, Int>()
        var duplicates = 0
        var allCount = 0
        BufferedReader(FileReader(File("result_7200K"))).use {
            var line = it.readLine()
            var curSize = -1
            while (line != null) {
                if ("SE" == line) {
                    if (curSize > 0) {
                        duplicates += curSize
                    }
                    if (map.containsKey(curSize)) {
                        val count = map[curSize]
                        map[curSize] = count!!.plus(1)
                    } else {
                        map[curSize] = 1
                    }
                    if (curSize > 100) {
                        println("$allCount $curSize")
                    }
                    curSize = -1
                } else {
                    curSize++
                }
                line = it.readLine()
                allCount++
            }
        }
        map.forEach { println("${it.key} ${it.value}") }
        println("Duplicates $duplicates)")
    }

    fun parseDirectoryLarge(path: String): UploadResult {
        logger.info("Start parsing directory.")
        val fileList = getFilesToParse(path)
        var total = 0
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val size = resultList.size
            total += size
            logger.info("resultList size is " + size)
            val uploadResult = documentService.processDocumentListLarge(resultList)
            val endTime = System.nanoTime()
            logger.info("The documents bulk is added in " + (endTime - startTime).toDouble() / 1000000000.0 + ". Total: " + total)
        }

        return result
    }
}

fun getFilesToParse(path: String): List<File> {
    val file = File(path)
    if (file.exists()) {
        if (file.isDirectory) {
            if (file.listFiles() != null) {
                val result = ArrayList<File>()
                file.listFiles().forEach { subFile ->
                    result.addAll(getFilesToParse(subFile.path))
                }
                return result
            }
        } else {
            return listOf(file)
        }
    }
    return emptyList()
}
