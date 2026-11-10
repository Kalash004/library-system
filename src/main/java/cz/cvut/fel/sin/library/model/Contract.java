package cz.cvut.fel.sin.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Author ↔ Publisher M:N link. It is an entity (not a {@code @ManyToMany})
 * because the link carries its own data: {@link #signedAt}.
 */
@Entity
@Table(name = "contract",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contract_author_publisher",
                columnNames = {"author_id", "publisher_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning side: this entity's table holds the foreign key. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @NotNull
    @Column(name = "signed_at", nullable = false)
    private LocalDate signedAt;

    public Contract(Author author, Publisher publisher, LocalDate signedAt) {
        this.author = author;
        this.publisher = publisher;
        this.signedAt = signedAt;
        // keep the in-memory inverse sides in sync with the owning side
        author.getContracts().add(this);
        publisher.getContracts().add(this);
    }
}
