package cz.cvut.fel.sin.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ISBN-13 with hyphens is at most 17 characters. Uniqueness is also checked in the service. */
    @NotBlank
    @Size(max = 17)
    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotNull
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Genre genre;

    /** N:1, owning side (FK {@code book.publisher_id}). */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    /** M:N, owning side: Hibernate writes the {@code authorship} rows from this collection. */
    @Size(min = 1, message = "a book must have at least one author")
    @ManyToMany
    @JoinTable(name = "authorship",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new HashSet<>();

    /** Changes only on the inverse side ({@code Author.books}) are not saved, so always go through here. */
    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
        if (publisher != null) {
            publisher.getBooks().add(this);
        }
    }
}
