package com.libsys;

import com.libsys.domain.Book;
import com.libsys.domain.Member;
import com.libsys.repo.BookRepository;
import com.libsys.repo.MemberRepository;
import com.libsys.service.LibraryService;
import com.libsys.util.HibernateUtil;

public class App {
    public static void main(String[] args) {
        // Warm up SessionFactory
        HibernateUtil.getSessionFactory();

        MemberRepository members = new MemberRepository();
        BookRepository books = new BookRepository();
        LibraryService svc = new LibraryService();

        // Demo seed (adjust as needed)
        Member m = new Member("Alice Nguyen", "alice@example.com");
        members.save(m);
        Book b = new Book("Domain-Driven Design", "ISBN-0001");
        b.setCategory("Software");
        books.save(b);

        var br = svc.borrowBook(m.getId(), b.getId(), 14);
        System.out.println("Borrowed ID=" + br.getId() + " for member " + m.getEmail());
    }
}
