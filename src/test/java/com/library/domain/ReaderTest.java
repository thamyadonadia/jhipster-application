package com.library.domain;

import static com.library.domain.ReaderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.library.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReaderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reader.class);
        Reader reader1 = getReaderSample1();
        Reader reader2 = new Reader();
        assertThat(reader1).isNotEqualTo(reader2);

        reader2.setId(reader1.getId());
        assertThat(reader1).isEqualTo(reader2);

        reader2 = getReaderSample2();
        assertThat(reader1).isNotEqualTo(reader2);
    }
}
