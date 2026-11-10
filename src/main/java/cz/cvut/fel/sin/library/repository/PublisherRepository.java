package cz.cvut.fel.sin.library.repository;

import cz.cvut.fel.sin.library.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
}
