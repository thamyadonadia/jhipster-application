package com.library.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.library.domain.enumeration.BookStatusEnum;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Book.
 */
@Table("book")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("title")
    private String title;

    @Column("publication_date")
    private LocalDate publicationDate;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    @Column("copies_owned")
    private Integer copiesOwned;

    @NotNull(message = "must not be null")
    @Column("status")
    private BookStatusEnum status;

    @org.springframework.data.annotation.Transient
    private Category category;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "books" }, allowSetters = true)
    private Set<Author> authors = new HashSet<>();

    @Column("category_id")
    private Long categoryId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Book id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public Book title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getPublicationDate() {
        return this.publicationDate;
    }

    public Book publicationDate(LocalDate publicationDate) {
        this.setPublicationDate(publicationDate);
        return this;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Integer getCopiesOwned() {
        return this.copiesOwned;
    }

    public Book copiesOwned(Integer copiesOwned) {
        this.setCopiesOwned(copiesOwned);
        return this;
    }

    public void setCopiesOwned(Integer copiesOwned) {
        this.copiesOwned = copiesOwned;
    }

    public BookStatusEnum getStatus() {
        return this.status;
    }

    public Book status(BookStatusEnum status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(BookStatusEnum status) {
        this.status = status;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.categoryId = category != null ? category.getId() : null;
    }

    public Book category(Category category) {
        this.setCategory(category);
        return this;
    }

    public Set<Author> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Book authors(Set<Author> authors) {
        this.setAuthors(authors);
        return this;
    }

    public Book addAuthor(Author author) {
        this.authors.add(author);
        return this;
    }

    public Book removeAuthor(Author author) {
        this.authors.remove(author);
        return this;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long category) {
        this.categoryId = category;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book)) {
            return false;
        }
        return getId() != null && getId().equals(((Book) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Book{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", publicationDate='" + getPublicationDate() + "'" +
            ", copiesOwned=" + getCopiesOwned() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
