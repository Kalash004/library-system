package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    /** Rule: a library may hold at most 5 copies of the same book. */
    long countByLibraryIdAndBookId(Long libraryId, Long bookId);
}
