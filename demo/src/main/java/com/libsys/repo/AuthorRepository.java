package com.libsys.repo;

import com.libsys.domain.Author;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.libsys.util.HibernateUtil;

import java.util.List;

public class AuthorRepository extends HibernateRepository<Author, Long> {
    public AuthorRepository() {
        super(Author.class);
    }

    public List<Author> searchByName(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Author> q = session.createQuery(
                    "from Author a where lower(a.name) like :kw", Author.class);
            q.setParameter("kw", "%" + keyword.toLowerCase() + "%");
            return q.getResultList();
        }
    }
}
