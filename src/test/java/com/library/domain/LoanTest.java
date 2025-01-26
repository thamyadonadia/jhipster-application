package com.library.domain;

import static com.library.domain.BookTestSamples.*;
import static com.library.domain.LoanTestSamples.*;
import static com.library.domain.ReaderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.library.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LoanTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Loan.class);
        Loan loan1 = getLoanSample1();
        Loan loan2 = new Loan();
        assertThat(loan1).isNotEqualTo(loan2);

        loan2.setId(loan1.getId());
        assertThat(loan1).isEqualTo(loan2);

        loan2 = getLoanSample2();
        assertThat(loan1).isNotEqualTo(loan2);
    }

    @Test
    void bookTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        loan.setBook(bookBack);
        assertThat(loan.getBook()).isEqualTo(bookBack);

        loan.book(null);
        assertThat(loan.getBook()).isNull();
    }

    @Test
    void memberTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Reader readerBack = getReaderRandomSampleGenerator();

        loan.setMember(readerBack);
        assertThat(loan.getMember()).isEqualTo(readerBack);

        loan.member(null);
        assertThat(loan.getMember()).isNull();
    }
}
