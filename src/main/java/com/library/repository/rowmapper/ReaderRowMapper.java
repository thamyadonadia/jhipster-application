package com.library.repository.rowmapper;

import com.library.domain.Reader;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Reader}, with proper type conversions.
 */
@Service
public class ReaderRowMapper implements BiFunction<Row, String, Reader> {

    private final ColumnConverter converter;

    public ReaderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Reader} stored in the database.
     */
    @Override
    public Reader apply(Row row, String prefix) {
        Reader entity = new Reader();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setFirstName(converter.fromRow(row, prefix + "_first_name", String.class));
        entity.setLastName(converter.fromRow(row, prefix + "_last_name", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setJoinedDate(converter.fromRow(row, prefix + "_joined_date", LocalDate.class));
        return entity;
    }
}
