package com.library.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class LoanTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Loan getLoanSample1() {
        return new Loan().id(1L);
    }

    public static Loan getLoanSample2() {
        return new Loan().id(2L);
    }

    public static Loan getLoanRandomSampleGenerator() {
        return new Loan().id(longCount.incrementAndGet());
    }
}
