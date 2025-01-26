package com.library.domain;

import static com.library.domain.AuthorTestSamples.*;
import static com.library.domain.BookTestSamples.*;
import static com.library.domain.CategoryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.library.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Book.class);
        Book book1 = getBookSample1();
        Book book2 = new Book();
        assertThat(book1).isNotEqualTo(book2);

        book2.setId(book1.getId());
        assertThat(book1).isEqualTo(book2);

        book2 = getBookSample2();
        assertThat(book1).isNotEqualTo(book2);
    }

    @Test
    void categoryTest() {
        Book book = getBookRandomSampleGenerator();
        Category categoryBack = getCategoryRandomSampleGenerator();

        book.setCategory(categoryBack);
        assertThat(book.getCategory()).isEqualTo(categoryBack);

        book.category(null);
        assertThat(book.getCategory()).isNull();
    }

    @Test
    void authorTest() {
        Book book = getBookRandomSampleGenerator();
        Author authorBack = getAuthorRandomSampleGenerator();

        book.addAuthor(authorBack);
        assertThat(book.getAuthors()).containsOnly(authorBack);

        book.removeAuthor(authorBack);
        assertThat(book.getAuthors()).doesNotContain(authorBack);

        book.authors(new HashSet<>(Set.of(authorBack)));
        assertThat(book.getAuthors()).containsOnly(authorBack);

        book.setAuthors(new HashSet<>());
        assertThat(book.getAuthors()).doesNotContain(authorBack);
    }
}
