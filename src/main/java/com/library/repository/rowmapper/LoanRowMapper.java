package com.library.repository.rowmapper;

import com.library.domain.Loan;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Loan}, with proper type conversions.
 */
@Service
public class LoanRowMapper implements BiFunction<Row, String, Loan> {

    private final ColumnConverter converter;

    public LoanRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Loan} stored in the database.
     */
    @Override
    public Loan apply(Row row, String prefix) {
        Loan entity = new Loan();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setLoanDate(converter.fromRow(row, prefix + "_loan_date", LocalDate.class));
        entity.setReturnDate(converter.fromRow(row, prefix + "_return_date", LocalDate.class));
        entity.setBookId(converter.fromRow(row, prefix + "_book_id", Long.class));
        entity.setMemberId(converter.fromRow(row, prefix + "_member_id", Long.class));
        return entity;
    }
}
