package ru.umeta.libraryintegration.service

import gnu.trove.set.hash.TLongHashSet
import org.apache.commons.io.output.FileWriterWithEncoding
import ru.umeta.libraryintegration.fs.EnrichedDocumentFsPersister
import ru.umeta.libraryintegration.json.UploadResult
import ru.umeta.libraryintegration.model.EnrichedDocumentLite
import ru.umeta.libraryintegration.parser.IXMLParser
import ru.umeta.libraryintegration.parser.ModsXMLParser
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*

/**
 * The main service to handle the integration logic
 * Created by k.kosolapov on 14/04/2015.
 */
object MainService : Closeable {

    val parser: IXMLParser = ModsXMLParser.Instance
    val documentService = DocumentService


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
            var iterationsLength = 0L
            var iterationsSetInter = 0L
            for (documentLite in documents) {
                val pivotId = documentLite.id
                if (!marked.contains(pivotId)) {
                    marked.add(pivotId)
                    writer.write("SECTION $i\n")
                    writer.write("$pivotId\n")
                    i++
                    val dfsResult = documentService.findEnrichedDocuments(documentLite, marked)
                    iterationsSetInter += dfsResult.iterationsSetInter
                    iterationsLength += dfsResult.iterationsLength
                    for (duplicate in dfsResult.component) {
                        val id = duplicate.id
                        marked.add(id)
                        writer.write("$id\n")
                    }
                }
                if (i % 100000 == 0) {
                    println(i);
                    println("Average Iterations on Sets ${iterationsSetInter/marked.size()}")
                    println("Average Iterations on Set Lengths ${iterationsLength/marked.size()}")
                    println("Marked ${marked.size()}")
                    println("----------------------------------------------------------------------")
                }
            }
            println("Average Iterations on Sets ${iterationsSetInter*1.0/marked.size()}")
            println("Average Iterations on Set Lengths ${iterationsLength*1.0/marked.size()}")
            println("Marked ${marked.size()}")
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

    override fun close() {
        documentService.close()
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

    fun collect() {
        var sections = 0
        var count = 0
        var sectionCount = 0
        var maxSelectionCount = 0
        Files.lines(File("duplicates.blob").toPath()).forEachOrdered {
            if (it.startsWith("SECTION")) {
                sections++
                maxSelectionCount = Math.max(maxSelectionCount, sectionCount)
                sectionCount = 0
            } else {
                count++
                sectionCount++
            }
        }
        println(sections)
        println(count)
        println(maxSelectionCount)
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