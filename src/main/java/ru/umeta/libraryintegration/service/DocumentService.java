package ru.umeta.libraryintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ctash on 28.04.2015.
 */
public class DocumentService {

    private static final String DEFAULT_PROTOCOL = "Z39.50";
    private static final int DUPLICATE_SIZE = 1000;

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

    private static final ConcurrentMap<Integer, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    public UploadResult processDocumentList(List<ParseResult> resultList, String protocolName) {
        int newEnriched = 0;
        int parsedDocs = 0;
        ArrayList<Thread> threadList = new ArrayList<>();
        int i = 0;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (ParseResult parseResult : checkNotNull(resultList)) {
            if (parseResult instanceof ModsParseResult) {
                try {
                    Document document = new Document();
                    ModsParseResult modsParseResult = (ModsParseResult) parseResult;

                    Thread thread = new Thread(() -> {
                        Integer publishYear = modsParseResult.getPublishYear();
                        if (publishYear != null && publishYear.equals(-1)) {
                            publishYear = null;
                        }
                        ReentrantLock lockForNullPublishYear = new ReentrantLock();
                        ReentrantLock lockForPublishYear = new ReentrantLock();

                        ReentrantLock mapLockForNullPublishYear = LOCK_MAP.putIfAbsent(-1, lockForNullPublishYear);
                        ReentrantLock mapLockForPublishYear = null;

                        if (mapLockForNullPublishYear != null) {
                            lockForNullPublishYear = mapLockForNullPublishYear;
                        }
                        if (publishYear != null) {
                            mapLockForPublishYear = LOCK_MAP.putIfAbsent(publishYear, lockForPublishYear);
                            if (mapLockForPublishYear != null) {
                                lockForPublishYear = mapLockForPublishYear;
                            }
                            lockForPublishYear.lock();
                        } else {

                        }


                        lockForNullPublishYear.lock();


                        document.setAuthor(stringHashService.getFromRepository(modsParseResult.getAuthor()));
                        document.setTitle(stringHashService.getFromRepository(modsParseResult.getTitle()));
                        document.setCreationTime(new Date());
                        String isbn = modsParseResult.getIsbn();
                        if (StringUtils.isEmpty(isbn)) {
                            isbn = null;
                        }
                        document.setIsbn(isbn);
                        document.setProtocol(protocolService.getFromRepository(protocolName == null ? DEFAULT_PROTOCOL : protocolName));
                        document.setPublishYear(publishYear);
                        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                            modsParseResult.getModsDefinition().save(outputStream);
                            document.setXml(new String(outputStream.toByteArray(),"UTF-8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        EnrichedDocument enrichedDocument = findEnrichedDocument(document, lockForNullPublishYear);
                        if (enrichedDocument != null) {
                            if (enrichedDocument.getPublishYear() == null) {
                                enrichedDocument.setPublishYear(document.getPublishYear());
                            }
                            if (enrichedDocument.getIsbn() == null) {
                                enrichedDocument.setIsbn(document.getIsbn());
                            }
                            mergeDocuments(modsParseResult, enrichedDocument);
                            enrichedDocumentDao.saveOrUpdate(enrichedDocument);
                        } else {
                            enrichedDocument = new EnrichedDocument();
                            enrichedDocument.setAuthor(document.getAuthor());
                            enrichedDocument.setTitle(document.getTitle());
                            enrichedDocument.setIsbn(document.getIsbn());
                            enrichedDocument.setXml(document.getXml());
                            enrichedDocument.setCreationTime(document.getCreationTime());
                            enrichedDocument.setPublishYear(document.getPublishYear());
                            enrichedDocument.setId(enrichedDocumentDao.save(enrichedDocument).longValue());
                            //newEnriched++;
                            document.setDistance(1.);
                        }
                        document.setEnrichedDocument(enrichedDocument);
                        documentDao.save(document);
                        unlockLockIfHold(lockForNullPublishYear);
                        unlockLockIfHold(lockForPublishYear);
                        //parsedDocs++;
                    });

                    threadList.add(thread);
                    executor.submit(thread);

                } catch (Exception e) {
                    System.err.println("ERROR. Failed to add a document with title {" +
                            parseResult.getTitle() + "}, author {" +
                            parseResult.getAuthor() + "}");
                }


            }
        }
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new UploadResult(parsedDocs, newEnriched);
    }

    private void mergeDocuments(ModsParseResult modsParseResult, EnrichedDocument enrichedDocument) {
        modsXMLParser.enrich(modsParseResult.getModsDefinition(),enrichedDocument);
    }

    private EnrichedDocument findEnrichedDocument(Document document, ReentrantLock lockForNullPublishYear) {

        //first check whether the document has isbn or not
        String isbn = document.getIsbn();
        Integer publishYear = document.getPublishYear();
        List<EnrichedDocument> nearDuplicates;
        if (isbn == null && publishYear == null) {
            // if it's null, we search through every record in the storage
            nearDuplicates = enrichedDocumentDao.getNearDuplicates(document);
        } else {

            // if it's not null, we search through record where isbn is the same
            if (publishYear == null) {
                nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithIsbn(document);

                if (nearDuplicates == null || nearDuplicates.size() == 0) {
                    // if it didn't find anything, search through record with null isbn.
                    nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithNullIsbn(document);
                }
            } else if (isbn == null) {
                nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithPublishYear(document);

            } else {
                //both publishYear and isbn is not null
                nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithIsbnAndPublishYear(document);

                if (nearDuplicates == null || nearDuplicates.size() == 0) {
                    // if it didn't find anything, search through record with null isbn.
                    nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithIsbn(document);

                    if (nearDuplicates == null || nearDuplicates.size() == 0) {
                        nearDuplicates = enrichedDocumentDao.getNearDuplicatesWithPublishYear(document);
                    }
                } else {
                    lockForNullPublishYear.unlock();
                }
            }

        }

        if (nearDuplicates != null && nearDuplicates.size() > 0) {

            double maxDistance = 0;
            double minDistance = 0.4;
            EnrichedDocument closestDocument = null;
            String titleValue = document.getTitle().getValue();
            String authorValue = document.getAuthor().getValue();
            for (EnrichedDocument nearDuplicate : nearDuplicates) {

                double titleDistance = stringHashService.distance(titleValue, nearDuplicate.getTitle().getValue());

                double authorDistance = stringHashService.distance(authorValue, nearDuplicate.getAuthor().getValue());

                double resultDistance = (titleDistance + authorDistance) / 2;

                if (resultDistance > maxDistance && resultDistance > minDistance) {
                    maxDistance = resultDistance;
                    closestDocument = nearDuplicate;
                }

            }
            if (closestDocument == null || closestDocument.getPublishYear() == null) {
                unlockLockIfHold(lockForNullPublishYear);
            }

            document.setDistance(maxDistance);
            return closestDocument;
        }

        return null;
    }

    private void unlockLockIfHold(ReentrantLock lockForNullPublishYear) {
        if (lockForNullPublishYear.getHoldCount() > 0) {
            lockForNullPublishYear.unlock();
        }
    }

    public List<ParseResult> addNoise(ParseResult parseResult, int saltLevel) {
        String author = parseResult.getAuthor();
        String title = parseResult.getTitle();

        int authorLength = author.length();
        int titleLength = title.length();

        parseResult.setIsbn(null);
        if (StringUtils.isEmpty(author) || StringUtils.isEmpty(title)) {
            return null;
        } else {
            ArrayList<ParseResult> resultList = new ArrayList<>();
            for (int i = 0; i < DUPLICATE_SIZE; i++) {
                ParseResult newParseResult = parseResult.clone();
                StringBuilder newAuthor = new StringBuilder(author);
                StringBuilder newTitle = new StringBuilder(title);
                for (int j = 0; j < saltLevel; j++) {
                    Random rnd = new Random();
                    int saltIndex = rnd.nextInt(authorLength);
                    newAuthor.setCharAt(saltIndex, '#');

                    saltIndex = new Random().nextInt(titleLength);
                    newTitle.setCharAt(saltIndex, '#');
                }
                newParseResult.setAuthor(newAuthor.toString());
                newParseResult.setTitle(newTitle.toString());
                resultList.add(newParseResult);
            }
            return resultList;
        }
    }
}
