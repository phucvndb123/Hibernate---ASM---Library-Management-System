package com.libsys.repo;

import com.libsys.domain.Book;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.libsys.util.HibernateUtil;

import java.util.List;

public class BookRepository extends HibernateRepository<Book, Long> {
    public BookRepository() {
        super(Book.class);
    }

    public List<Book> topBorrowed(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> q = session.createQuery(
                    "from Book b order by b.borrowCount desc", Book.class);
            q.setMaxResults(limit);
            return q.getResultList();
        }
    }

    public List<Book> search(String titleOrCategory) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> q = session.createQuery(
                    "from Book b where lower(b.title) like :kw or lower(b.category) like :kw", Book.class);
            q.setParameter("kw", "%" + titleOrCategory.toLowerCase() + "%");
            return q.getResultList();
        }
    }
}
