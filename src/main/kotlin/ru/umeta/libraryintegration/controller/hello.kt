package ru.umeta.libraryintegration.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView
import ru.umeta.libraryintegration.dao.DocumentDao
import ru.umeta.libraryintegration.dao.EnrichedDocumentDao
import ru.umeta.libraryintegration.dao.StringHashDao
import ru.umeta.libraryintegration.model.Document
import ru.umeta.libraryintegration.service.ProtocolService
import ru.umeta.libraryintegration.service.StringHashService
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ctash on 9/5/2015.
 */
Controller
RequestMapping("/kt")
public class KotlinController {

    @RequestMapping(method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun handlePost(request: HttpServletRequest, response: HttpServletResponse): String {
        return "kthello";
    }

}

Controller
RequestMapping("/gen")
public class GenerationController {

    Autowired
    public var documentDao: DocumentDao? = null

    Autowired
    public val stringHashService: StringHashService? = null

    Autowired
    public val protocolService: ProtocolService? = null

    Autowired
    public val enrichedDocumentDao: EnrichedDocumentDao? = null

    Autowired
    public val stringHashDao: StringHashDao? = null

    private val xml: String = "LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +
            "LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"LAAAAAAAAAAAAAAAAAAAAA" +
            "asddsadd" +
            "as" +
            "dsad" +
            "as" +
            "ds" +
            "ad" +
            "asd" +
            "asd" +
            "a" +
            "da" +
            "sd" +
            "asd" +
            "as" +
            "d" +
            "asd" +
            "asdadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "asdsa" +
            "d" +
            "das" +
            "d" +
            "sdasdsadsadasdasdsada" +
            "ad" +
            "asd" +
            "asdadadsadasdasdadasdasdasdasdsadad" +
            "ad" +
            "as" +
            "da" +
            "sdadsdasdasdasdasdsdadsdadasdasdasddsaaaaaaaaaaaaaaaaaaaaaaasdsdasd" +
            "asdasdasdadadsdasdsadasdasdadadadsdada" +
            "dsa" +
            "dasd" +
            "asd" +"addasdadsdadsad";

    @RequestMapping
    fun handle(): String {

        val enrichedDocument = enrichedDocumentDao?.get(93562L)
        val documentList = ArrayList<Document>()
        for (i in 1..1000) {
            val document = Document()

            document.setAuthor(stringHashDao?.get(93560L))
            document.setTitle(stringHashDao?.get(93572L))
            document.setCreationTime(Date())
            document.setIsbn("2133")
            document.setProtocol(protocolService?.getFromRepository("Z39.50"));
            document.setXml(xml)
            document.setDistance(1.0)
            document.setEnrichedDocument(enrichedDocument)
            documentList.add(document)

        }
        documentDao?.save(documentList)
        return "generated"
    }

}
