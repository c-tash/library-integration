package ru.umeta.libraryintegration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.umeta.libraryintegration.json.ParseResult;
import ru.umeta.libraryintegration.json.UploadResult;
import ru.umeta.libraryintegration.model.Document;
import ru.umeta.libraryintegration.parser.IXMLParser;
import ru.umeta.libraryintegration.parser.ModsXMLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The main service to handle the integration logic
 * Created by k.kosolapov on 14/04/2015.
 */
@Service
public class MainService {

    private final IXMLParser parser;

    @Autowired
    private DocumentService documentService;

    public MainService(IXMLParser parser) {
        this.parser = parser;
    }

    public UploadResult parseDirectory(String path) throws InterruptedException {

        List<File> fileList = getFilesToParse(path);
        UploadResult result = new UploadResult(0, 0);
        for (File file : fileList) {
            long startTime = System.nanoTime();
            List<ParseResult> resultList = parser.parse(file);
            long parseTime = System.nanoTime();
            System.out.println("The documents bulk parsed in " +
                    (double) (parseTime - startTime) / 1000000000.0);
            System.out.println("resultList size is " + resultList.size());

            UploadResult uploadResult = documentService.processDocumentList(resultList, null);
            long endTime = System.nanoTime();
            System.out.println("The documents bulk is added in "
                    + (double) (endTime - startTime) / 1000000000.0);
            result.setParsedDocs(result.getParsedDocs() + uploadResult.getParsedDocs());
            result.setNewEnriched(result.getNewEnriched()
                    + uploadResult.getNewEnriched());
        }
        return result;
    }

    private static List<File> getFilesToParse(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    List<File> result = new ArrayList<>();
                    Arrays.asList(file.listFiles()).stream().forEach(
                            subFile -> result.addAll(getFilesToParse(subFile.getPath())));
                    return result;
                }
            } else {
                return Collections.singletonList(file);
            }
        }
        return Collections.emptyList();
    }

    public static UploadResult parseDirectoryStatic(String path) throws InterruptedException {

        List<File> fileList = getFilesToParse(path);
        UploadResult result = new UploadResult(0, 0);
        for (File file : fileList) {
            long startTime = System.nanoTime();
            List<ParseResult> resultList = new ModsXMLParser().parse(file);
            long parseTime = System.nanoTime();
            System.out.println("The documents bulk parsed in " +
                    (double) (parseTime - startTime) / 1000000000.0);
            System.out.println("resultList size is " + resultList.size());
        }
        return result;
    }


    public UploadResult parseDirectoryBalance(String path, int saltLevel) {
        List<File> fileList = getFilesToParse(path);
        UploadResult result = new UploadResult(0, 0);
        for (File file : fileList) {
            long startTime = System.nanoTime();
            List<ParseResult> resultList = parser.parse(file);
            long parseTime = System.nanoTime();
            System.out.println("The documents bulk parsed in " + (double) (parseTime - startTime) / 1000000000.0);
            System.out.println("resultList size is " + resultList.size());
            for (ParseResult parseResult : resultList) {
                List<ParseResult> saltedResult = documentService.addNoise(parseResult, saltLevel);
                if (saltedResult == null) {
                    System.out.println("The parsed result either had no authors or the title was blank.");
                    continue;
                }
                UploadResult uploadResult = documentService.processDocumentList(saltedResult, null);
                System.out.println("The result with salt of level " + saltLevel + " is " + uploadResult.getNewEnriched());

            }

        }
        return result;
    }

    public static void main(String[] args) {
        Map<Long, Document> documents = new HashMap<>();
        long i = 0L;
        try {
            for (; i < 200000000; i++) {
                Document document = new Document();

                //document.setAuthor(stringHashService.getFromRepository(modsParseResult.getAuthor()));
                //document.setTitle(stringHashService.getFromRepository(modsParseResult.getTitle()));
                document.setCreationTime(new Date());
                String isbn = "12331232313";
                document.setIsbn(isbn);
                //                    document.setProtocol(protocolService.getFromRepository(protocolName == null ? DEFAULT_PROTOCOL : protocolName));
                document.setPublishYear(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
    }
}
