package ru.umeta.libraryintegration.service;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.Nullable;
import ru.umeta.libraryintegration.inmemory.InMemoryRepository;
import ru.umeta.libraryintegration.model.EnrichedDocumentLite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.umeta.libraryintegration.service.StringHashServiceKt.distanceWithTheorems;
import static ru.umeta.libraryintegration.service.StringHashServiceKt.getBigrammWeighted;

/**
 * Created by ctash on 18.03.2016.
 */
public class JavaDuplicateService {

    public static final String RESULT_FILE_NAME = "result";
    private final InMemoryRepository repository;

    private final StringHashService stringService;

    public JavaDuplicateService(InMemoryRepository repository, StringHashService stringService) {
        this.repository = repository;
        this.stringService = stringService;
    }

    public void parse() throws IOException {
        simpleParse();
    }

    public void simpleParse() {
        Section section = new Section();
        long i = 1;


        try (Writer writer = Files.newBufferedWriter(Paths.get(RESULT_FILE_NAME), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(new FileReader(new File("duplicates.blob")))) {
            for (String line = br.readLine();line != null; line = br.readLine()) {
                if (line.contains("SE")) {
                    if (!section.list.isEmpty()) {
                        section = handleSection(section, writer);
                        if (section == null)
                            continue;
                    }
                } else {
                    int id = Integer.parseInt(line);
                    if (id < 25000000) {
                        section.list.add(repository.getDocStorage()[id]);
                    }
                }
                i++;
                if (i % 1000000 == 0) {
                    System.out.println("Collecting " + i / 1000000 + "M\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Theorems results");
            System.out.println("All " + StringHashServiceKt.getDistanceCounterAll());
            System.out.println("T1 " + StringHashServiceKt.getDistanceCounterOptim1());
            System.out.println("T2 " + StringHashServiceKt.getDistanceCounterOptim2());
        }

    }

    @Nullable
    private Section handleSection(Section section, Writer writer) {
        EnrichedDocumentLite headDoc = section.getList().get(0);
        int idHead = headDoc.getId();
        if (repository.getDocMarked()[idHead]) {
            return null;
        } else {
            repository.getDocMarked()[idHead] = true;
        }
        section.head = headDoc;
        String authorHead = repository.getString(headDoc.getAuthorId());
        List<Bigramm> aTokensHead = getBigrammWeighted(authorHead);
        String titleHead = repository.getString(headDoc.getTitleId());
        List<Bigramm> tTokensHead = getBigrammWeighted(titleHead);
        EnrichedDocumentLite[] results = new EnrichedDocumentLite[section.getList().size()];
        AtomicInteger count = new AtomicInteger(1);
        section.getList().parallelStream().forEach(docIt -> {
            int id = docIt.getId();
            if (repository.getDocMarked()[id]) {
                return;
            }

            if (docIt == headDoc) {
                return;
            }
            String aStringIt = repository.getString(docIt.getAuthorId());
            double aRatio = distanceWithTheorems(authorHead, aStringIt, aTokensHead, null, 0.7);
            if (aRatio >= 0.7) {
                String tStringIt = repository.getString(docIt.getTitleId());
                double tRatio = distanceWithTheorems(titleHead, tStringIt, tTokensHead, null, 0.7);
                if (tRatio >= 0.7) {
                    EnrichedDocumentLite clone = docIt.clone();
                    results[count.getAndIncrement()] = clone;
                    clone.setRatio((aRatio + tRatio) / 2);
                    repository.getDocMarked()[id] = true;
                }
            }
        });
        section.list = Arrays.stream(results)
                .parallel()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            writer.write("SE\n");
            writer.write(section.head.getId() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        section.list.forEach(itResult -> {
            try {
                writer.write(itResult.getId() + "|" + itResult.getRatio() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        section = new Section();
        return section;
    }

    public void parseDebug() {
        Section section = new Section();
        long i = 1;
        try (Writer writer = new FileWriterWithEncoding("result", "UTF-8");
                BufferedReader br = new BufferedReader(new FileReader(new File("duplicates.blob")))) {
            for (String line = br.readLine();line != null; line = br.readLine()) {
                if (line.contains("SE")) {
                    System.out.println("debug1");
                    if (!section.list.isEmpty()) {
                        System.out.println("debug2");
                        EnrichedDocumentLite headDoc = section.getList().get(0);
                        int idHead = headDoc.getId();
                        if (repository.getDocMarked()[idHead]) {
                            System.out.println("debug3");
                            section = new Section();
                            continue;
                        } else {
                            repository.getDocMarked()[idHead] = true;
                        }
                        System.out.println("debug4");
                        section.head = headDoc;
                        System.out.println("debug5");
                        String authorHead = repository.getString(headDoc.getAuthorId());
                        Set<String> aTokensHead = stringService.getSimHashTokens(authorHead);
                        String titleHead = repository.getString(headDoc.getTitleId());
                        Set<String> tTokensHead = stringService.getSimHashTokens(titleHead);
                        EnrichedDocumentLite[] results = new EnrichedDocumentLite[section.getList().size()];
                        AtomicInteger count = new AtomicInteger(1);
                        System.out.println("debug6");
                        section.getList().parallelStream().forEach(docIt -> {
                            int id = docIt.getId();
                            if (repository.getDocMarked()[id]) {
                                System.out.println("debug7");
                                return;
                            }

                            if (docIt == headDoc) {
                                System.out.println("debug8");
                                return;
                            }
                            System.out.println("debug9");
                            String aStringIt = repository.getString(docIt.getAuthorId());
                            Set<String> aTokensIt = stringService.getSimHashTokens(aStringIt);
                            double authorTokensRatio = aTokensHead.size() * 1.0 / aTokensIt.size();
                            System.out.println("debug10");
                            if (authorTokensRatio > 1) {
                                authorTokensRatio = 1.0 / authorTokensRatio;
                            }

                            if (authorTokensRatio >= 0.4) {
                                System.out.println("debug11");
                                String tStringIt = repository.getString(docIt.getTitleId());
                                Set<String> tTokensIt = stringService.getSimHashTokens(tStringIt);
                                double titleTokensRatio = tTokensHead.size() * 1.0 / tTokensIt.size();
                                if (titleTokensRatio > 1) {
                                    titleTokensRatio = 1.0 / titleTokensRatio;
                                }
                                System.out.println("debug12");
                                if (authorTokensRatio + titleTokensRatio >= 0.7 * 2) {
                                    double ratio = stringService.distance(aTokensHead, aTokensIt) + stringService.distance(tTokensHead, tTokensIt);
                                    if (ratio >= 1.4) {
                                        System.out.println("debug13");
                                        EnrichedDocumentLite clone = docIt.clone();
                                        results[count.getAndIncrement()] = clone;
                                        clone.setRatio(ratio / 2);
                                        repository.getDocMarked()[id] = true;
                                        System.out.println("debug14");
                                    }
                                }
                            }
                        });
                        System.out.println("debug15");
                        section.list = Arrays.stream(results).parallel()
                                .filter(it -> it != null)
                                .collect(Collectors.toList());
                        try {
                            System.out.println("debug16");
                            writer.write("SE\n");
                            writer.write(section.head.getId() + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        section.list.forEach(itResult -> {
                            try {
                                System.out.println("debug17");
                                writer.write(itResult.getId() + "|" + itResult.getRatio() + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        System.out.println("debug18");
                        section = new Section();
                    }
                } else {
                    System.out.println("debug19");
                    int id = Integer.parseInt(line);
                    if (id < 25000000) {
                        section.list.add(repository.getDocStorage()[id]);
                    }
                }
                i++;
                if (i % 1000000 == 0) {
                    System.out.println("debug20");
                    System.out.println("Collecting " + i / 1000000 + "M\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates") public void parserLegacy() {
        Section section = new Section();
        long i = 1;
        try (Writer writer = new FileWriterWithEncoding("result", "UTF-8");
                BufferedReader br = new BufferedReader(new FileReader(new File("duplicates.blob")))) {
            for (String line = br.readLine();line != null; line = br.readLine()) {
                if (line.contains("SE")) {
                    int headFromSection = Integer.parseInt(line.substring(8));
                    if (!section.list.isEmpty()) {
                        EnrichedDocumentLite headDoc = section.getHead();
                        int idHead = headDoc.getId();
                        if (repository.getDocMarked()[idHead]) {
                            section = new Section();
                            continue;
                        } else {
                            repository.getDocMarked()[idHead] = true;
                        }
                        section.head = headDoc;
                        String authorHead = repository.getString(headDoc.getAuthorId());
                        Set<String> aTokensHead = stringService.getSimHashTokens(authorHead);
                        String titleHead = repository.getString(headDoc.getTitleId());
                        Set<String> tTokensHead = stringService.getSimHashTokens(titleHead);
                        EnrichedDocumentLite[] results = new EnrichedDocumentLite[section.getList().size()];
                        AtomicInteger count = new AtomicInteger(1);
                        section.getList().parallelStream().forEach(docIt -> {
                            int id = docIt.getId();
                            if (repository.getDocMarked()[id]) {
                                return;
                            }

                            if (docIt == headDoc) {
                                return;
                            }
                            String aStringIt = repository.getString(docIt.getAuthorId());
                            Set<String> aTokensIt = stringService.getSimHashTokens(aStringIt);
                            double authorTokensRatio = aTokensHead.size() * 1.0 / aTokensIt.size();
                            if (authorTokensRatio > 1) {
                                authorTokensRatio = 1.0 / authorTokensRatio;
                            }

                            if (authorTokensRatio >= 0.4) {
                                String tStringIt = repository.getString(docIt.getTitleId());
                                Set<String> tTokensIt = stringService.getSimHashTokens(tStringIt);
                                double titleTokensRatio = tTokensHead.size() * 1.0 / tTokensIt.size();
                                if (titleTokensRatio > 1) {
                                    titleTokensRatio = 1.0 / titleTokensRatio;
                                }
                                if (authorTokensRatio + titleTokensRatio >= 0.7 * 2) {
                                    double ratio = stringService.distance(aTokensHead, aTokensIt) + stringService.distance(tTokensHead, tTokensIt);
                                    if (ratio >= 1.4) {
                                        EnrichedDocumentLite clone = docIt.clone();
                                        results[count.getAndIncrement()] = clone;
                                        clone.setRatio(ratio / 2);
                                        repository.getDocMarked()[id] = true;
                                    }
                                }
                            }
                        });
                        section.list = Arrays.stream(results).parallel()
                                .filter(it -> it != null)
                                .collect(Collectors.toList());
                        try {
                            writer.write("SE\n");
                            writer.write(section.head.getId() + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        section.list.forEach(itResult -> {
                            try {
                                writer.write(itResult.getId() + "|" + itResult.getRatio() + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        section = new Section();
                    }
                    section.head = repository.getDocStorage()[headFromSection];
                } else {
                    String[] split = line.split("|");
                    int id = Integer.parseInt(split[0]);
                    if (id < 25000000) {
                        section.list.add(repository.getDocStorage()[id]);
                    }
                }
                i++;
                if (i % 10000000 == 0) {
                    System.out.println("Collecting " + i / 1000000 + "M\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class Section {
        int headInt;

        EnrichedDocumentLite head;

        List<EnrichedDocumentLite> list = Lists.mutable.<EnrichedDocumentLite>empty();

        public List<EnrichedDocumentLite> getList() {
            return list;
        }

        public void setList(List<EnrichedDocumentLite> list) {
            this.list = list;
        }

        public EnrichedDocumentLite getHead() {
            return head;
        }

        public void setHead(EnrichedDocumentLite head) {
            this.head = head;
        }
    }

}
