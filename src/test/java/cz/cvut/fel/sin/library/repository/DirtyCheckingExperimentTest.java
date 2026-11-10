package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Author;
import cz.cvut.fel.sin.library.model.Book;
import cz.cvut.fel.sin.library.model.Genre;
import cz.cvut.fel.sin.library.model.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

/**
 * Experiment: when does Hibernate send an UPDATE without us calling save()?
 * Run it and read the SQL between the >>> lines.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DirtyCheckingExperimentTest {

    @Autowired TestEntityManager em;
    @Autowired BookRepository books;

    @Test
    void experiment() {
        Long id = createBook();
        em.clear(); // empty the persistence context: next read must come from the DB

        System.out.println(">>> 1. load the book");
        Book book = books.findById(id).orElseThrow();   // managed + Hibernate keeps a snapshot

        System.out.println(">>> 2. change the title, then flush (no save!)");
        book.setTitle("New title");
        em.flush();

        System.out.println(">>> 3. set the title to the SAME value again, then flush");
        book.setTitle("New title");
        em.flush();

        System.out.println(">>> 4. detach the book, change it, then flush");
        em.detach(book);                                 // no longer managed
        book.setTitle("Detached change");
        em.flush();

        System.out.println(">>> 5. merge the detached book back, then flush");
        books.save(book);                                // save() on an existing entity = merge
        em.flush();

        System.out.println(">>> end");
    }

    private Long createBook() {
        Publisher p = new Publisher();
        p.setName("P");
        p.setAddress("A");
        em.persist(p);

        Author a = new Author();
        a.setFirstName("F");
        a.setLastName("L");
        a.setEmail("dirty@example.com");
        em.persist(a);

        Book b = new Book();
        b.setIsbn("978-0-00-000000-9");
        b.setTitle("Old title");
        b.setPublicationDate(LocalDate.of(2024, 1, 1));
        b.setGenre(Genre.FANTASY);
        b.setPublisher(p);
        b.addAuthor(a);
        em.persistAndFlush(b);
        return b.getId();
    }
}
