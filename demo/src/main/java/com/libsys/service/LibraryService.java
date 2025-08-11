package com.libsys.service;

import com.libsys.domain.Book;
import com.libsys.domain.Borrowing;
import com.libsys.domain.Member;
import com.libsys.repo.BookRepository;
import com.libsys.repo.BorrowingRepository;
import com.libsys.repo.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;

public class LibraryService {
    private final BookRepository books = new BookRepository();
    private final MemberRepository members = new MemberRepository();
    private final BorrowingRepository borrows = new BorrowingRepository();

    public Borrowing borrowBook(Long memberId, Long bookId, int days) {
        Optional<Book> b = books.findById(bookId);
        Optional<Member> m = members.findById(memberId);
        if (b.isEmpty() || m.isEmpty()) throw new IllegalArgumentException("Invalid member/book");
        Book book = b.get();
        if (!book.isAvailable()) throw new IllegalStateException("Book is not available");
        book.setAvailable(false);
        book.setBorrowCount(book.getBorrowCount() + 1);
        books.save(book);
        Borrowing br = new Borrowing(m.get(), book, LocalDate.now().plusDays(days));
        return borrows.save(br);
    }

    public Borrowing returnBook(Long borrowingId) {
        Borrowing br = borrows.findById(borrowingId).orElseThrow();
        br.setReturnedOn(LocalDate.now());
        br.getBook().setAvailable(true);
        books.save(br.getBook());
        return borrows.save(br);
    }
}
