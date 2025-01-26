package com.library.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BookTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Book getBookSample1() {
        return new Book().id(1L).title("title1").copiesOwned(1);
    }

    public static Book getBookSample2() {
        return new Book().id(2L).title("title2").copiesOwned(2);
    }

    public static Book getBookRandomSampleGenerator() {
        return new Book().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).copiesOwned(intCount.incrementAndGet());
    }
}
