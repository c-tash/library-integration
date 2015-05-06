package ru.umeta.libraryintegration.dao;

import org.hibernate.SessionFactory;
import ru.umeta.libraryintegration.model.EnrichedDocument;

/**
 * Created by k.kosolapov on 05/05/2015.
 */
public class EnrichedDocumentDao extends AbstractDao<EnrichedDocument> {

    public EnrichedDocumentDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
