package ru.umeta.libraryintegration.dao;

import org.hibernate.SessionFactory;
import ru.umeta.libraryintegration.model.Document;

/**
 * Created by k.kosolapov on 05/05/2015.
 */
public class DocumentDao extends AbstractDao<Document> {

    public DocumentDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
