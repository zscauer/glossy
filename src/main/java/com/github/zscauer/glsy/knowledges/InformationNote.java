package com.github.zscauer.glsy.knowledges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.zscauer.glsy.common.ActiveRecord;
import com.github.zscauer.glsy.common.IncompleteRepositoryOperationException;
import com.github.zscauer.glsy.common.PageableRequestParams;
import com.github.zscauer.glsy.common.PageableSearchRequestParams;
import io.quarkus.qute.TemplateData;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.FormParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@TemplateData
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class InformationNote extends ActiveRecord {

    @Nullable
    @FormParam("information-note-id")
    UUID id;
    @FormParam("information-note-name")
    String name;
    @FormParam("information-note-description")
    String description;
    boolean actual = true;
    List<Tag> tags;

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public String toString() {
        return """
                Name: %s
                Description: %s
                Actual: %s
                Tags: %s""".formatted(name, description, actual, stringTags());
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public InformationNote save() throws IncompleteRepositoryOperationException {
        checkNonNullableFields(getClass(), name, description);

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

    public InformationNote addTag(final Tag tag) {
        checkNonNullableFields(InformationNote.class, id());
        checkNonNullableFields(Tag.class, tag.id());

        addTagToInformationNote(id, tag.id());

        return find(id);
    }

    public static InformationNote deleteTag(final UUID informationNoteId, final UUID tagId) {
        checkNonNullableFields(InformationNote.class, informationNoteId);
        checkNonNullableFields(Tag.class, tagId);

        deleteTagFromInformationNote(informationNoteId, tagId);

        return find(informationNoteId);
    }

    private String stringTags() {
        final StringBuilder tagsBuilder = new StringBuilder();
        tagsBuilder.append("[");
        if (!tags().isEmpty()) {
            final Iterator<Tag> tagIterator = tags().iterator();
            Tag iterableTag;
            while (tagIterator.hasNext()) {
                iterableTag = tagIterator.next();
                if (tagIterator.hasNext()) {
                    tagsBuilder.append("%s, ".formatted(iterableTag.name()));
                } else {
                    tagsBuilder.append("%s".formatted(iterableTag.name()));
                }
            }
        }
        tagsBuilder.append("]");

        return tagsBuilder.toString();
    }

    // +++ Repository +++
    private void insert() throws IncompleteRepositoryOperationException {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_INSERT)) {
            statement.setObject(1, id);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setBoolean(4, actual);

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
            statement.setString(2, description);
            statement.setBoolean(3, actual);
            statement.setObject(4, id);

            statement.executeUpdate();
        } catch (final SQLException e) {
            if (CONSTRAINT_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IncompleteRepositoryOperationException();
            } else {
                log.error(e.getMessage());
            }
        }
    }

    public static boolean exists(final UUID informationNoteId) {
        if (informationNoteId == null) {
            return false;
        }

        boolean exists = false;
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_EXISTS)) {
            statement.setObject(1, informationNoteId);

            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                exists = rs.getBoolean("result");
            }
            closeResultSet(rs);
        } catch (final SQLException e) {
            log.error(e.getMessage());
        }

        return exists;
    }

    @Nullable
    public static InformationNote find(final UUID informationNoteId) {
        if (informationNoteId == null) {
            return null;
        }

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND)) {
            statement.setObject(1, informationNoteId);

            return map(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static List<InformationNote> findAll(final PageableSearchRequestParams requestParams) {
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

    public static List<InformationNote> findAllWithTag(final UUID tagId,
                                                       final PageableRequestParams requestParams) {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_ALL_WITH_TAG_PAGEABLE)) {

            statement.setObject(1, tagId);
            statement.setShort(2, requestParams.pageSize());
            statement.setInt(3, requestParams.pageSize() * (requestParams.page() - 1));

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private static void addTagToInformationNote(final UUID informationNoteId, final UUID tagId) {
        if (containsTag(informationNoteId, tagId)) {
            return;
        }

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_ADD_TAG)) {

            statement.setObject(1, informationNoteId);
            statement.setObject(2, tagId);

            statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage());
        }
    }

    private static void deleteTagFromInformationNote(final UUID informationNoteId, final UUID tagId) {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_DELETE_TAG)) {

            statement.setObject(1, informationNoteId);
            statement.setObject(2, tagId);

            statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage());
        }
    }

    private static boolean containsTag(final UUID informationNoteId, final UUID tagId) {
        boolean contains = false;
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_CONTAINS_TAG)) {
            statement.setObject(1, informationNoteId);
            statement.setObject(2, tagId);

            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                contains = rs.getBoolean("result");
            }
            closeResultSet(rs);
        } catch (final SQLException e) {
            log.error(e.getMessage());
        }

        return contains;
    }
    // --- Repository ---

    // +++ Mapping +++
    @Nullable
    private static InformationNote map(final ResultSet rs) throws SQLException {
        InformationNote found = null;
        if (rs.next()) {
            found = readFromResultSet(rs);
        }
        closeResultSet(rs);

        return found;
    }

    private static List<InformationNote> mapToList(final ResultSet rs) throws SQLException {
        final List<InformationNote> found = new ArrayList<>();
        while (rs.next()) {
            found.add(readFromResultSet(rs));
        }
        closeResultSet(rs);

        return found;
    }

    private static InformationNote readFromResultSet(final ResultSet rs) throws SQLException {
        final UUID noteId = rs.getObject("information_note_id", UUID.class);
        return new InformationNote()
                .id(noteId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .actual(rs.getBoolean("actual"))
                .tags(Tag.findAllOfInformationCard(noteId));
    }
    // --- Mapping ---

    // +++ Statements +++
    private static final String STATEMENT_EXISTS = """
            SELECT COUNT(information_note_id) > 0 AS result
            FROM information_notes
            WHERE information_note_id = ?
            """;

    private static final String STATEMENT_INSERT = """
            INSERT INTO information_notes (information_note_id, name, description, actual)
            VALUES (?, ?, ?, ?)
            """;

    private static final String STATEMENT_UPDATE = """
            UPDATE information_notes
            SET name = ?, description = ?, actual = ?
            WHERE information_note_id = ?
            """;

    private static final String STATEMENT_FIND = """
            SELECT * FROM information_notes
            WHERE information_note_id = ?
            """;

    private static final String STATEMENT_FIND_ALL_PAGEABLE = """
            WITH search_str AS (
                SELECT CONCAT('%%', LOWER(CAST(? AS varchar)), '%%')
            )
            SELECT * FROM information_notes
            WHERE (SELECT * FROM search_str) IS null OR (
                LOWER(name) LIKE (SELECT * FROM search_str) OR
                LOWER(description) LIKE (SELECT * FROM search_str)
                )
            ORDER BY name
            LIMIT ? + 1 OFFSET ?
            """;

    private static final String STATEMENT_FIND_ALL_WITH_TAG_PAGEABLE = """
            SELECT information_notes.* FROM information_notes
            JOIN information_notes_tags ON information_notes.information_note_id = information_notes_tags.information_note_id
            AND information_notes_tags.tag_id = ?
            LIMIT ? + 1 OFFSET ?
            """;

    private static final String STATEMENT_CONTAINS_TAG = """
            SELECT COUNT(tag_id) > 0 AS result
            FROM information_notes_tags
            WHERE information_note_id = ? AND tag_id = ?
            """;

    private static final String STATEMENT_ADD_TAG = """
            INSERT INTO information_notes_tags (information_note_id, tag_id)
            VALUES (?, ?)
            """;

    private static final String STATEMENT_DELETE_TAG = """
            DELETE FROM information_notes_tags
            WHERE information_note_id = ? AND tag_id = ?
            """;
    // --- Statements ---

}
