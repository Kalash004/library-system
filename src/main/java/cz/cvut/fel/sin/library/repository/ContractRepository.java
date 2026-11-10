package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    /** Rule: every author of a new book must have a contract with the book's publisher. */
    boolean existsByAuthorIdAndPublisherId(Long authorId, Long publisherId);
}
