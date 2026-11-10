package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;

/** No surrounding transaction: behaves like renameBook() WITHOUT @Transactional. */
@SpringBootTest
class NoTransactionExperimentTest {
    @Autowired BookRepository books; @Autowired AuthorRepository authors; @Autowired PublisherRepository publishers;
    @Autowired TransactionTemplate tx;

    @Test
    void renameWithoutTransaction() {
        Publisher p = new Publisher(); p.setName("P"); p.setAddress("A"); publishers.save(p);
        Author a = new Author(); a.setFirstName("F"); a.setLastName("L"); a.setEmail("notx@example.com"); authors.save(a);
        Book b = new Book(); b.setIsbn("978-0-00-000000-7"); b.setTitle("Old title");
        b.setPublicationDate(LocalDate.of(2024,1,1)); b.setGenre(Genre.FANTASY); b.setPublisher(p); b.addAuthor(a);
        books.save(b);
        try {
            // --- body of renameBook() without @Transactional ---
            Book book = books.findById(b.getId()).orElseThrow();
            book.setTitle("New title");
            // --- method ends ---
            System.out.println(">>> without @Transactional, title in DB: " + books.findById(b.getId()).orElseThrow().getTitle());

            // --- same body WITH a transaction ---
            tx.executeWithoutResult(s -> {
                Book managed = books.findById(b.getId()).orElseThrow();
                managed.setTitle("New title");
            });
            System.out.println(">>> with @Transactional, title in DB: " + books.findById(b.getId()).orElseThrow().getTitle());
        } finally {
            tx.executeWithoutResult(s -> { books.deleteById(b.getId()); authors.deleteById(a.getId()); publishers.deleteById(p.getId()); });
        }
    }
}
