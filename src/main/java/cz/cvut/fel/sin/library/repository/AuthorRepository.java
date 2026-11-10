package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    boolean existsByEmail(String email);
}
