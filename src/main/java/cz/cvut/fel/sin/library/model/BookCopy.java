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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One physical copy of a {@link Book} owned by a {@link Library}.
 * Rule (service layer): at most 5 copies per (library, book).
 */
@Entity
@Table(name = "book_copy")
@Getter
@Setter
@NoArgsConstructor
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "inventory_number", nullable = false, unique = true, length = 50)
    private String inventoryNumber;

    /** Unidirectional N:1: we don't need Book.copies yet. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    public BookCopy(String inventoryNumber, Book book, Library library) {
        this.inventoryNumber = inventoryNumber;
        this.book = book;
        this.library = library;
        library.getCopies().add(this);
    }
}
