package ru.umeta.libraryintegration.dao;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.umeta.libraryintegration.model.Protocol;

/**
 * Created by k.kosolapov on 05/05/2015.
 */
@Repository
public class ProtocolDao extends AbstractDao<Protocol> {

    public ProtocolDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Protocol persist(Protocol protocol) {
        return super.persist(protocol);
    }
}
