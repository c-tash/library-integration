package ru.umeta.libraryintegration.dao;

import org.hibernate.*;
import org.springframework.stereotype.Repository;
import ru.umeta.libraryintegration.model.StringHash;
import ru.umeta.libraryintegration.util.Generics;


import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by k.kosolapov on 30.04.2015.
 */
public abstract class AbstractDao<E>{

    private final SessionFactory sessionFactory;
    private final Class<?> entityClass;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory    a session provider
     */
    protected AbstractDao(SessionFactory sessionFactory) {
        this.sessionFactory = checkNotNull(sessionFactory);
        this.entityClass = Generics.getTypeParameter(getClass());
    }

    /**
     * Returns the current {@link Session}.
     *
     * @return the current session
     */
    protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Creates a new {@link Criteria} query for {@code <E>}.
     *
     * @return a new {@link Criteria} query
     * @see Session#createCriteria(Class)
     */
    protected Criteria criteria() {
        return currentSession().createCriteria(entityClass);
    }

    /**
     * Returns a named {@link Query}.
     *
     * @param queryName the name of the query
     * @return the named query
     * @see Session#getNamedQuery(String)
     */
    protected Query namedQuery(String queryName) throws HibernateException {
        return currentSession().getNamedQuery(checkNotNull(queryName));
    }

    /**
     * Returns the entity class managed by this DAO.
     *
     * @return the entity class managed by this DAO
     */
    @SuppressWarnings("unchecked")
    public Class<E> getEntityClass() {
        return (Class<E>) entityClass;
    }

    /**
     * Convenience method to return a single instance that matches the criteria, or null if the
     * criteria returns no results.
     *
     * @param criteria the {@link Criteria} query to run
     * @return the single result or {@code null}
     * @throws HibernateException if there is more than one matching result
     * @see Criteria#uniqueResult()
     */
    @SuppressWarnings("unchecked")
    protected E uniqueResult(Criteria criteria) throws HibernateException {
        return (E) checkNotNull(criteria).uniqueResult();
    }

    /**
     * Convenience method to return a single instance that matches the query, or null if the query
     * returns no results.
     *
     * @param query the query to run
     * @return the single result or {@code null}
     * @throws HibernateException if there is more than one matching result
     * @see Query#uniqueResult()
     */
    @SuppressWarnings("unchecked")
    protected E uniqueResult(Query query) throws HibernateException {
        return (E) checkNotNull(query).uniqueResult();
    }

    /**
     * Get the results of a {@link Criteria} query.
     *
     * @param criteria the {@link Criteria} query to run
     * @return the list of matched query results
     * @see Criteria#list()
     */
    @SuppressWarnings("unchecked")
    protected List<E> list(Criteria criteria) throws HibernateException {
        return checkNotNull(criteria).list();
    }

    /**
     * Get the results of a query.
     *
     * @param query the query to run
     * @return the list of matched query results
     * @see Query#list()
     */
    @SuppressWarnings("unchecked")
    protected List<E> list(Query query) throws HibernateException {
        return checkNotNull(query).list();
    }

    /**
     * Return the persistent instance of {@code <E>} with the given identifier, or {@code null} if
     * there is no such persistent instance. (If the instance, or a proxy for the instance, is
     * already associated with the session, return that instance or proxy.)
     *
     * @param id an identifier
     * @return a persistent instance or {@code null}
     * @throws HibernateException
     * @see Session#get(Class, Serializable)
     */
    @SuppressWarnings("unchecked")
    public E get(Number id) {
        Session session = currentSession();
        Transaction transaction = null;
        E result = null;
        try {
            transaction = session.beginTransaction();
            result = (E) (session.get(entityClass, checkNotNull(id)));
        } catch (Exception e) {

        }
        finally {
            if (transaction != null) {
                transaction.commit();
            }
        }
        return result;
    }

    /**
     * Either save or update the given instance, depending upon resolution of the unsaved-value
     * checks (see the manual for discussion of unsaved-value checking).
     * <p/>
     * This operation cascades to associated instances if the association is mapped with
     * <tt>cascade="save-update"</tt>.
     *
     * @param entity a transient or detached instance containing new or updated state
     * @throws HibernateException
     * @see Session#saveOrUpdate(Object)
     */
    public void saveOrUpdate(E entity) throws HibernateException {
        currentSession().saveOrUpdate(checkNotNull(entity));
    }

    @SuppressWarnings("unchecked")
    public Number save(E entity) {
        return (Number) currentSession().save(checkNotNull(entity));
    }

    @SuppressWarnings("unchecked")
    public void save(List<E> entityList) {
        for (E entity : entityList) {
            currentSession().save(checkNotNull(entity));
        }
    }

    @SuppressWarnings("unchecked")
    public E get(String value) {
        return (E) currentSession().bySimpleNaturalId(entityClass).load(value);
    }

}
