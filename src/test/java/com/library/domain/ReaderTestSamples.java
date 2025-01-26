package com.library.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ReaderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Reader getReaderSample1() {
        return new Reader().id(1L).firstName("firstName1").lastName("lastName1").email("email1");
    }

    public static Reader getReaderSample2() {
        return new Reader().id(2L).firstName("firstName2").lastName("lastName2").email("email2");
    }

    public static Reader getReaderRandomSampleGenerator() {
        return new Reader()
            .id(longCount.incrementAndGet())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString());
    }
}
