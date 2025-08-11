package com.libsys.repo;

import com.libsys.domain.Borrowing;
import com.libsys.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class BorrowingRepository extends HibernateRepository<Borrowing, Long> {
    public BorrowingRepository() {
        super(Borrowing.class);
    }

    public List<Borrowing> overdueSince(LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> q = session.createQuery(
                    "from Borrowing br where br.returnedOn is null and br.dueOn < :date", Borrowing.class);
            q.setParameter("date", date);
            return q.getResultList();
        }
    }
}
