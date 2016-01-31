package ru.umeta.libraryintegration.service

import gnu.trove.set.hash.TLongHashSet
import org.apache.commons.io.output.FileWriterWithEncoding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.umeta.libraryintegration.json.UploadResult
import ru.umeta.libraryintegration.parser.IXMLParser
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * The main service to handle the integration logic
 * Created by k.kosolapov on 14/04/2015.
 */
@Component
class MainService
@Autowired
constructor(val parser: IXMLParser, val documentService: DocumentService) {

    @Throws(InterruptedException::class)
    fun parseDirectory(path: String): UploadResult {
        println("Start parsing directory.")
        val fileList = getFilesToParse(path)
        var total = 0
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val size = resultList.size
            total += size
            println("resultList size is " + size)
            val uploadResult = documentService.processDocumentList(resultList, null)
            val endTime = System.nanoTime()
            println("The documents bulk is added in " + (endTime - startTime).toDouble() / 1000000000.0 + ". Total: " + total)
            result.parsedDocs = result.parsedDocs + uploadResult.parsedDocs
            result.newEnriched = result.newEnriched + uploadResult.newEnriched
        }

        return result
    }

    fun find() {
        println("Start finding duplicates.")
        (FileWriterWithEncoding(File("duplicates.blob"), Charset.forName("UTF-8"), false).use {
            writer ->
            val documents = documentService.getDocuments()
            val marked = TLongHashSet();
            var i = 1;
            for (documentLite in documents) {
                if (!marked.contains(documentLite.id)) {
                    writer.write("SECTION $i\n")
                    i++
                    val duplicates = documentService.findEnrichedDocuments(documentLite)
                    for (duplicate in duplicates) {
                        val id = duplicate.id
                        marked.add(id)
                        writer.write("$id\n")
                    }
                }
                if (i % 100000 == 0) {
                    println(i);
                }
            }
        })
    }

    fun parseDirectoryBalance(path: String, saltLevel: Int): UploadResult {
        val fileList = getFilesToParse(path)
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val parseTime = System.nanoTime()
            println("The documents bulk parsed in " + (parseTime - startTime).toDouble() / 1000000000.0)
            println("resultList size is " + resultList.size)
            for (parseResult in resultList) {
                val saltedResult = documentService.addNoise(parseResult, saltLevel)
                if (saltedResult == null) {
                    println("The parsed result either had no authors or the title was blank.")
                    continue
                }
                val uploadResult = documentService.processDocumentList(saltedResult, null)
                println("The result with salt of level " + saltLevel + " is " + uploadResult.newEnriched)

            }

        }
        return result
    }

    fun parseDirectoryInit(path: String): Any {
        println("Start parsing directory.")
        val fileList = getFilesToParse(path)
        var total = 0
        val result = UploadResult(0, 0)
        for (file in fileList) {
            val startTime = System.nanoTime()
            val resultList = parser.parse(file)
            val size = resultList.size
            total += size
            println("resultList size is " + size)
            val uploadResult = documentService.processDocumentListInit(resultList, null)
            val endTime = System.nanoTime()
            println("The documents bulk is added in " + (endTime - startTime).toDouble() / 1000000000.0 + ". Total: " + total)
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