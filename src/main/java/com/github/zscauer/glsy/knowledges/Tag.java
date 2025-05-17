package com.github.zscauer.glsy.knowledges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.zscauer.glsy.common.ActiveRecord;
import com.github.zscauer.glsy.common.IncompleteRepositoryOperationException;
import com.github.zscauer.glsy.common.PageableSearchRequestParams;
import io.quarkus.qute.TemplateData;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString(exclude = {"id"})
@TemplateData
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Tag extends ActiveRecord {

    UUID id;
    String name;

    @Override
    public boolean isNew() {
        return id == null;
    }

    public static boolean exists(final String name) {
        return !findByName(name, true).isEmpty();
    }

    public static boolean exists(final UUID id) {
        return exists(id, STATEMENT_EXISTS);
    }

    @Nonnull
    public static Tag getOfName(final String name) {
        final List<Tag> found = findByName(name, true);
        if (found.isEmpty()) {
            return new Tag().name(name).save();
        }
        return found.getFirst();
    }

    @Nonnull
    public Tag save() throws IncompleteRepositoryOperationException {
        checkNonNullableFields(getClass(), name);

        if (isNew()) {
            id = UUID.randomUUID();
            insert();
        } else {
            if (exists(id)) {
                update();
            } else {
                insert();
            }
        }

        return this;
    }

    // +++ Repository +++
    private void insert() {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_INSERT)) {
            statement.setObject(1, id);
            statement.setString(2, name);

            statement.executeUpdate();
        } catch (final SQLException e) {
            if (CONSTRAINT_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IncompleteRepositoryOperationException();
            } else {
                log.error(e.getMessage());
            }
        }
    }

    private void update() {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_UPDATE)) {
            statement.setString(1, name);
            statement.setObject(2, id);

            statement.executeUpdate();
        } catch (final SQLException e) {
            if (CONSTRAINT_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IncompleteRepositoryOperationException();
            } else {
                log.error(e.getMessage());
            }
        }
    }

    @Nullable
    public static Tag find(final UUID tagId) {
        if (tagId == null) {
            return null;
        }

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND)) {
            statement.setObject(1, tagId);

            return map(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static List<Tag> findAll(final PageableSearchRequestParams requestParams) {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_ALL_PAGEABLE)) {

            statement.setString(1, requestParams.searchString());
            statement.setShort(2, requestParams.pageSize());
            statement.setInt(3, requestParams.pageSize() * (requestParams.page() - 1));

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<Tag> findByName(final String name, boolean exactName) {
        final String searchString = exactName ? name : formatForSearchLikeExpression(name);

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_BY_NAME)) {
            statement.setString(1, searchString);

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<Tag> findAllOfInformationCard(final UUID informationCardId) {
        if (informationCardId == null) {
            return Collections.emptyList();
        }

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_ALL_OF_INFORMATION_CARD)) {
            statement.setObject(1, informationCardId);

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
    // --- Repository ---

    // +++ Mapping +++
    @Nullable
    private static Tag map(final ResultSet rs) throws SQLException {
        Tag found = null;
        if (rs.next()) {
            found = readFromResultSet(rs);
        }
        closeResultSet(rs);

        return found;
    }

    private static List<Tag> mapToList(final ResultSet rs) throws SQLException {
        final List<Tag> found = new ArrayList<>();
        while (rs.next()) {
            found.add(readFromResultSet(rs));
        }
        closeResultSet(rs);

        return found;
    }

    private static Tag readFromResultSet(final ResultSet rs) throws SQLException {
        return new Tag()
                .id(rs.getObject("tag_id", UUID.class))
                .name(rs.getString("name"));
    }
    // --- Mapping ---

    // +++ Statements +++
    private static final String STATEMENT_EXISTS = """
            SELECT COUNT(tag_id) > 0 FROM tags
            WHERE tag_id = ?
            """;

    private static final String STATEMENT_INSERT = """
            INSERT INTO tags (tag_id, name)
            VALUES (?, ?)
            """;

    private static final String STATEMENT_UPDATE = """
            UPDATE tags
            SET name = ?
            WHERE tag_id = ?
            """;

    private static final String STATEMENT_FIND = """
            SELECT * FROM tags
            WHERE tag_id = ?
            """;

    private static final String STATEMENT_FIND_ALL_PAGEABLE = """
            WITH search_str AS (
                SELECT CONCAT('%%', LOWER(REPLACE(CAST(? AS varchar), ' ', '')), '%%')
            )
            SELECT * FROM tags
            WHERE (SELECT * FROM search_str) IS null OR LOWER(REPLACE(name, ' ', '')) LIKE (SELECT * FROM search_str)
            ORDER BY name
            LIMIT ? + 1 OFFSET ?
            """;

    private static final String STATEMENT_FIND_BY_NAME = """
            WITH search_str AS (
                SELECT LOWER(REPLACE(CAST(? AS varchar), ' ', ''))
            )
            SELECT * FROM tags
            WHERE LOWER(REPLACE(name, ' ', '')) LIKE (SELECT * FROM search_str)
            """;

    private static final String STATEMENT_FIND_ALL_OF_INFORMATION_CARD = """
            SELECT tags.* FROM tags
            JOIN information_notes_tags ON tags.tag_id = information_notes_tags.tag_id
            AND information_notes_tags.information_note_id = ?
            """;
    // --- Statements ---

}
