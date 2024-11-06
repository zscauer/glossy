package com.github.zscauer.glsy.common;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Keeps data source and have common methods.
 * All database represented classes should extends this class to have access to data source.
 */
@Slf4j
public abstract class ActiveRecord {

    private static DataSource DATA_SOURCE;

    protected static final String CONSTRAINT_UNIQUE_VIOLATION = "23505";

    /**
     * Datasource could be injected only once.
     *
     * @param ds datasource to inject
     */
    public static void setDatasource(@Nonnull final DataSource ds) {
        if (DATA_SOURCE == null) {
            DATA_SOURCE = ds;
        }
    }

    protected static DataSource dataSource() {
        return DATA_SOURCE;
    }

    /**
     * Exists or not database representation.
     *
     * @return {@code true} if object not persisted yet
     */
    public boolean isNew() {
        return false;
    }

    /**
     * Check all values to not contain null.
     *
     * @param recordClass checkable class
     * @param values      checkable values
     * @throws IllegalStateException if some values are null
     */
    protected static void checkNonNullableFields(final Class<? extends ActiveRecord> recordClass,
                                                 final Object... values) throws IllegalStateException {
        try {
            for (final Object value : values) {
                Objects.requireNonNull(value);
            }
        } catch (final Exception e) {
            if (e instanceof NullPointerException) {
                throw new IllegalStateException("Some of required fields of active record '%s' contains null values."
                        .formatted(recordClass));
            } else {
                throw e;
            }
        }
    }

    /**
     * Universal method to check existence of object in database.
     *
     * @param id              object id to find
     * @param existsStatement find expression to execute
     * @return {@code true} if result of statementExpression is true
     */
    protected static boolean exists(final Object id, final String existsStatement) {
        boolean exists = false;
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(existsStatement)) {
            statement.setObject(1, id);

            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
            closeResultSet(rs);

            return exists;
        } catch (final SQLException e) {
            log.error(e.getMessage());
            return exists;
        }
    }

    protected static String formatForSearchLikeExpression(final String value) {
        return String.format("%%%s%%", value);
    }

    protected static void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                log.error("Unable to close result set: {}", e.getMessage());
            }
        }
    }

}
