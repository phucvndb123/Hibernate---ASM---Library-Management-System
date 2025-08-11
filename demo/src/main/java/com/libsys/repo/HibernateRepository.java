package com.libsys.repo;

import com.libsys.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class HibernateRepository<T, ID extends Serializable> implements CrudRepository<T, ID> {
    private final Class<T> type;

    public HibernateRepository(Class<T> type) {
        this.type = type;
    }

    @Override
    public T save(T entity) {
        return executeInTx(session -> {
            session.saveOrUpdate(entity);
            return entity;
        });
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(type, id));
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> q = session.createQuery("from " + type.getSimpleName(), type);
            return q.getResultList();
        }
    }

    @Override
    public void deleteById(ID id) {
        executeInTx(session -> {
            T entity = session.get(type, id);
            if (entity != null) session.delete(entity);
            return null;
        });
    }

    @Override
    public void delete(T entity) {
        executeInTx(session -> {
            session.delete(entity);
            return null;
        });
    }

    private <R> R executeInTx(Function<Session, R> fn) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            R res = fn.apply(session);
            tx.commit();
            return res;
        } catch (RuntimeException ex) {
            if (tx != null && tx.getStatus().canRollback()) tx.rollback();
            throw ex;
        }
    }
}
