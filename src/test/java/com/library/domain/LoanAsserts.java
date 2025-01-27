package com.library.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class LoanAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertLoanAllPropertiesEquals(Loan expected, Loan actual) {
        assertLoanAutoGeneratedPropertiesEquals(expected, actual);
        assertLoanAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertLoanAllUpdatablePropertiesEquals(Loan expected, Loan actual) {
        assertLoanUpdatableFieldsEquals(expected, actual);
        assertLoanUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertLoanAutoGeneratedPropertiesEquals(Loan expected, Loan actual) {
        assertThat(expected)
            .as("Verify Loan auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertLoanUpdatableFieldsEquals(Loan expected, Loan actual) {
        assertThat(expected)
            .as("Verify Loan relevant properties")
            .satisfies(e -> assertThat(e.getLoanDate()).as("check loanDate").isEqualTo(actual.getLoanDate()))
            .satisfies(e -> assertThat(e.getReturnDate()).as("check returnDate").isEqualTo(actual.getReturnDate()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertLoanUpdatableRelationshipsEquals(Loan expected, Loan actual) {
        assertThat(expected)
            .as("Verify Loan relationships")
            .satisfies(e -> assertThat(e.getBook()).as("check book").isEqualTo(actual.getBook()))
            .satisfies(e -> assertThat(e.getMember()).as("check member").isEqualTo(actual.getMember()));
    }
}
