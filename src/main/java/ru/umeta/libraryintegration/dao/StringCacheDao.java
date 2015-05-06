package ru.umeta.libraryintegration.dao;

import org.hibernate.SessionFactory;
import ru.umeta.libraryintegration.model.StringCache;

/**
 * Created by k.kosolapov on 05/05/2015.
 */
public class StringCacheDao extends AbstractDao<StringCache> {

    public StringCacheDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
