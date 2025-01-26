package com.library.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Loan.
 */
@Table("loan")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("loan_date")
    private LocalDate loanDate;

    @Column("return_date")
    private LocalDate returnDate;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "category", "authors" }, allowSetters = true)
    private Book book;

    @org.springframework.data.annotation.Transient
    private Reader member;

    @Column("book_id")
    private Long bookId;

    @Column("member_id")
    private Long memberId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Loan id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getLoanDate() {
        return this.loanDate;
    }

    public Loan loanDate(LocalDate loanDate) {
        this.setLoanDate(loanDate);
        return this;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }

    public Loan returnDate(LocalDate returnDate) {
        this.setReturnDate(returnDate);
        return this;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Book getBook() {
        return this.book;
    }

    public void setBook(Book book) {
        this.book = book;
        this.bookId = book != null ? book.getId() : null;
    }

    public Loan book(Book book) {
        this.setBook(book);
        return this;
    }

    public Reader getMember() {
        return this.member;
    }

    public void setMember(Reader reader) {
        this.member = reader;
        this.memberId = reader != null ? reader.getId() : null;
    }

    public Loan member(Reader reader) {
        this.setMember(reader);
        return this;
    }

    public Long getBookId() {
        return this.bookId;
    }

    public void setBookId(Long book) {
        this.bookId = book;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(Long reader) {
        this.memberId = reader;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Loan)) {
            return false;
        }
        return getId() != null && getId().equals(((Loan) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Loan{" +
            "id=" + getId() +
            ", loanDate='" + getLoanDate() + "'" +
            ", returnDate='" + getReturnDate() + "'" +
            "}";
    }
}
