package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Author;
import cz.cvut.fel.sin.library.model.Book;
import cz.cvut.fel.sin.library.model.BookCopy;
import cz.cvut.fel.sin.library.model.Contract;
import cz.cvut.fel.sin.library.model.Genre;
import cz.cvut.fel.sin.library.model.Library;
import cz.cvut.fel.sin.library.model.Publisher;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Round-trips every relationship through the real PostgreSQL schema created by Liquibase.
 * Each test runs in a transaction that is rolled back, so the dev database stays clean.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use PostgreSQL, not an embedded DB
class MappingTest {

    @Autowired TestEntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired BookRepository books;
    @Autowired ContractRepository contracts;
    @Autowired BookCopyRepository copies;

    @Test
    void persistsWholeModelAndDerivedQueriesWork() {
        Author author = author("jane@example.com");
        Publisher publisher = publisher();
        em.persist(new Contract(author, publisher, LocalDate.of(2024, 1, 15)));

        Book book = book("978-80-01-00000-1", publisher);
        book.addAuthor(author);
        em.persist(book);

        Library library = new Library();
        library.setName("City Library");
        library.setAddress("Main St 1");
        em.persist(library);
        em.persist(new BookCopy("INV-0001", book, library));
        em.persist(new BookCopy("INV-0002", book, library));

        em.flush();  // send the INSERTs now
        em.clear();  // forget the in-memory objects, so the reads below hit the database

        assertThat(books.existsByIsbn("978-80-01-00000-1")).isTrue();
        assertThat(contracts.existsByAuthorIdAndPublisherId(author.getId(), publisher.getId())).isTrue();
        assertThat(copies.countByLibraryIdAndBookId(library.getId(), book.getId())).isEqualTo(2);

        // the owning side (Book.authors) wrote the join-table row
        assertThat(jdbc.queryForObject("SELECT count(*) FROM authorship", Integer.class)).isEqualTo(1);
        // EnumType.STRING stores the name, not the position
        assertThat(jdbc.queryForObject("SELECT genre FROM book", String.class)).isEqualTo("FANTASY");

        Book loaded = books.findById(book.getId()).orElseThrow();
        assertThat(loaded.getAuthors()).extracting(Author::getEmail).containsExactly("jane@example.com");
    }

    @Test
    void bookWithoutAuthorIsRejected() {
        Book book = book("978-80-01-00000-2", publisher());
        assertThatThrownBy(() -> em.persistAndFlush(book))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("at least one author");
    }

    private Author author(String email) {
        Author a = new Author();
        a.setFirstName("Jane");
        a.setLastName("Doe");
        a.setEmail(email);
        return em.persist(a);
    }

    private Publisher publisher() {
        Publisher p = new Publisher();
        p.setName("Albatros");
        p.setAddress("Prague");
        return em.persist(p);
    }

    private static Book book(String isbn, Publisher publisher) {
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle("The Book");
        b.setPublicationDate(LocalDate.of(2024, 3, 1));
        b.setGenre(Genre.FANTASY);
        b.setPublisher(publisher);
        return b;
    }
}
