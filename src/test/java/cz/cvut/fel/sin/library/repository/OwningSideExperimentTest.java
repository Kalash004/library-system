package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OwningSideExperimentTest {
    @Autowired TestEntityManager em;
    @Autowired JdbcTemplate jdbc;

    @Test
    void experiment() {
        Publisher p = new Publisher(); p.setName("P"); p.setAddress("A"); em.persist(p);
        Author jane = author("jane@example.com");
        Author tom  = author("tom@example.com");

        Book book = new Book();
        book.setIsbn("978-0-00-000000-1"); book.setTitle("T");
        book.setPublicationDate(LocalDate.of(2024, 1, 1)); book.setGenre(Genre.FANTASY);
        book.setPublisher(p);
        book.addAuthor(jane);
        em.persistAndFlush(book);
        System.out.println(">>> rows after jane: " + count());

        tom.getBooks().add(book);          // inverse side only
        em.flush();
        System.out.println(">>> rows after tom.getBooks().add(book): " + count());

        book.getAuthors().add(tom);        // owning side
        em.flush();
        System.out.println(">>> rows after book.getAuthors().add(tom): " + count());
    }

    private Author author(String email) {
        Author a = new Author(); a.setFirstName("F"); a.setLastName("L"); a.setEmail(email);
        return em.persist(a);
    }
    private Integer count() { return jdbc.queryForObject("SELECT count(*) FROM authorship", Integer.class); }
}
