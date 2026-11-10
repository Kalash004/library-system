package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Rule: the ISBN of a new book must not exist yet. */
    boolean existsByIsbn(String isbn);
}
