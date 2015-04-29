package ru.umeta.libraryintegration.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by k.kosolapov on 27.04.2015.
 */
@Entity
@Table(name = "Document")
public class Document {

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "title_string_id")
    private StringCache title;

    @ManyToOne
    @JoinColumn(name = "author_string_id")
    private StringCache author;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "xml")
    private String xml;

    @Column(name = "creation_time")
    private Date creationTime;

    @ManyToOne
    @JoinColumn(name = "protocol_id")
    private Protocol protocol;

    @ManyToOne
    @JoinColumn(name = "enriched_id")
    private EnrichedDocument enrichedDocument;
}