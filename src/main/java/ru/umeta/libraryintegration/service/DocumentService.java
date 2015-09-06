package ru.umeta.libraryintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import ru.umeta.libraryintegration.dao.DocumentDao;
import ru.umeta.libraryintegration.dao.EnrichedDocumentDao;
import ru.umeta.libraryintegration.json.ModsParseResult;
import ru.umeta.libraryintegration.json.ParseResult;
import ru.umeta.libraryintegration.json.UploadResult;
import ru.umeta.libraryintegration.model.Document;
import ru.umeta.libraryintegration.model.EnrichedDocument;
import ru.umeta.libraryintegration.parser.ModsXMLParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ctash on 28.04.2015.
 */
public class DocumentService {

    private static final String DEFAULT_PROTOCOL = "Z39.50";

    @Autowired
    private StringHashService stringHashService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private EnrichedDocumentDao enrichedDocumentDao;

    @Autowired
    private DocumentDao documentDao;

    @Autowired
    private ModsXMLParser modsXMLParser;

    public UploadResult processDocumentList(List<ParseResult> resultList, String protocolName) {
        int newEnriched = 0;
        int parsedDocs = 0;

        for (ParseResult parseResult : checkNotNull(resultList)) {
            if (parseResult instanceof ModsParseResult) {

                Document document = new Document();
                ModsParseResult modsParseResult = (ModsParseResult) parseResult;

                long startTime = System.nanoTime();
                document.setAuthor(stringHashService.getFromRepository(modsParseResult.getAuthor()));
                long authorSearchTime = System.nanoTime();
                System.out.println("authorSearchTime" + (double) (authorSearchTime - startTime) / 1000000000.0);
                document.setTitle(stringHashService.getFromRepository(modsParseResult.getTitle()));
                long titleSearchTime = System.nanoTime();
                System.out.println("titleSearchTime" + (double) (titleSearchTime - authorSearchTime) / 1000000000.0);
                document.setCreationTime(new Date());
                document.setIsbn(modsParseResult.getIsbn());
                document.setProtocol(protocolService.getFromRepository(protocolName == null ? DEFAULT_PROTOCOL : protocolName));
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    modsParseResult.getModsDefinition().save(outputStream);
                    document.setXml(new String(outputStream.toByteArray(),"UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                long findEnrichedStartTime = System.nanoTime();
                EnrichedDocument enrichedDocument = findEnrichedDocument(document);
                long findEnrichedEndTime = System.nanoTime();
                System.out.println("findEnrichedEndTime" + (double) (findEnrichedEndTime - findEnrichedStartTime) / 1000000000.0);
                if (enrichedDocument != null) {
                    long mergeStartTime = System.nanoTime();
                    mergeDocuments(modsParseResult, enrichedDocument);
                    enrichedDocumentDao.saveOrUpdate(enrichedDocument);
                    long mergeEndTime = System.nanoTime();
                    System.out.println("mergeEndTime" + (double) (mergeEndTime - mergeStartTime) / 1000000000.0);
                } else {
                    long creationTime = System.nanoTime();
                    enrichedDocument = new EnrichedDocument();
                    enrichedDocument.setAuthor(document.getAuthor());
                    enrichedDocument.setTitle(document.getTitle());
                    enrichedDocument.setIsbn(document.getIsbn());
                    enrichedDocument.setXml(document.getXml());
                    enrichedDocument.setCreationTime(document.getCreationTime());
                    enrichedDocument.setId(enrichedDocumentDao.save(enrichedDocument).longValue());
                    long creationEndTime = System.nanoTime();
                    System.out.println("new file save time" + (double) (creationEndTime - creationTime) / 1000000000.0);
                    newEnriched++;
                    document.setDistance(1.);
                }
                long createDocTime = System.nanoTime();
                document.setEnrichedDocument(enrichedDocument);
                documentDao.save(document);
                long createDocEndTime = System.nanoTime();
                System.out.println("new file save time" + (double) (createDocEndTime - createDocTime) / 1000000000.0);
                parsedDocs++;
            }
        }
        return new UploadResult(parsedDocs, newEnriched);
    }

    private void mergeDocuments(ModsParseResult modsParseResult, EnrichedDocument enrichedDocument) {
        modsXMLParser.enrich(modsParseResult.getModsDefinition(),enrichedDocument);
    }

    private EnrichedDocument findEnrichedDocument(Document document) {

        //first check whether the document has isbn or not
        String isbn = document.getIsbn();
        List<EnrichedDocument> nearDuplicates;
        if (isbn == null) {
            // if it's null, we search through every record in the storage
            nearDuplicates = enrichedDocumentDao.getNearDuplicates(document);
        } else {
            // if it's not null, we search through record where isbn is the same
            nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithIsbn(document);

            if (nearDuplicates == null || nearDuplicates.size() == 0) {
                // if it didn't find anything, search through record with null isbn.
                nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithNullIsbn(document);
            }
        }

        if (nearDuplicates != null && nearDuplicates.size() > 0) {

            double maxDistance = 0;
            EnrichedDocument closestDocument = null;
            String titleValue = document.getTitle().getValue();
            String authorValue = document.getAuthor().getValue();
            for (EnrichedDocument nearDuplicate : nearDuplicates) {

                double titleDistance = stringHashService.distance(titleValue, nearDuplicate.getTitle().getValue());

                double authorDistance = stringHashService.distance(authorValue, nearDuplicate.getAuthor().getValue());

                double resultDistance = (titleDistance + authorDistance) / 2;

                if (resultDistance > maxDistance) {
                    maxDistance = resultDistance;
                    closestDocument = nearDuplicate;
                }

            }
            document.setDistance(maxDistance);
            return closestDocument;
        }

        return null;
    }
}
