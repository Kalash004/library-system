package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Author;
import cz.cvut.fel.sin.library.model.Book;
import cz.cvut.fel.sin.library.model.Contract;
import cz.cvut.fel.sin.library.model.Genre;
import cz.cvut.fel.sin.library.model.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Experiment: the same M:N idea mapped two ways.
 *  - Book ↔ Author   : @ManyToMany, join table "authorship" (no class for the row)
 *  - Author ↔ Publisher: join ENTITY Contract (a class for the row, with signedAt)
 * Read the SQL between the >>> lines.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JoinEntityExperimentTest {

    @Autowired TestEntityManager em;

    @Test
    void experiment() {
        Publisher albatros = publisher("Albatros");
        Publisher argo = publisher("Argo");
        Author jane = author("jane@example.com");
        Author tom = author("tom@example.com");
        Book book = book(albatros, jane);
        em.flush();

        System.out.println(">>> 1. @ManyToMany: add an author = add to a collection");
        book.addAuthor(tom);
        em.flush();

        System.out.println(">>> 2. @ManyToMany: remove an author = remove from the collection");
        book.getAuthors().remove(tom);
        tom.getBooks().remove(book);
        em.flush();

        System.out.println(">>> 3. join entity: a link is an object you persist, with its own data");
        em.persist(new Contract(jane, albatros, LocalDate.of(2020, 5, 1)));
        em.persist(new Contract(jane, argo, LocalDate.of(2024, 9, 1)));
        em.flush();

        System.out.println(">>> 4. join entity: query BY the link's data (impossible with @ManyToMany)");
        List<String> recent = em.getEntityManager().createQuery(
                        "select c.publisher.name from Contract c "
                                + "where c.author = :author and c.signedAt >= :since", String.class)
                .setParameter("author", jane)
                .setParameter("since", LocalDate.of(2023, 1, 1))
                .getResultList();
        System.out.println(">>> publishers jane signed with since 2023: " + recent);

        System.out.println(">>> 5. join entity: the unique (author, publisher) constraint");
        try {
            em.persist(new Contract(jane, albatros, LocalDate.of(2025, 1, 1)));
            em.flush();
        } catch (Exception e) {
            System.out.println(">>> rejected by the DB: " + e.getClass().getSimpleName());
        }
        System.out.println(">>> end");
    }

    private Publisher publisher(String name) {
        Publisher p = new Publisher();
        p.setName(name);
        p.setAddress("Prague");
        return em.persist(p);
    }

    private Author author(String email) {
        Author a = new Author();
        a.setFirstName("F");
        a.setLastName("L");
        a.setEmail(email);
        return em.persist(a);
    }

    private Book book(Publisher publisher, Author author) {
        Book b = new Book();
        b.setIsbn("978-0-00-000000-5");
        b.setTitle("T");
        b.setPublicationDate(LocalDate.of(2024, 1, 1));
        b.setGenre(Genre.FANTASY);
        b.setPublisher(publisher);
        b.addAuthor(author);
        return em.persist(b);
    }
}
