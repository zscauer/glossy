package com.github.zscauer.glsy.knowledges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.zscauer.glsy.common.ActiveRecord;
import com.github.zscauer.glsy.common.PageableRequestParams;
import com.github.zscauer.glsy.common.PageableSearchRequestParams;
import io.quarkus.qute.TemplateData;
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
@ToString
@TemplateData
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TagStatistics extends ActiveRecord {

    Tag tag;
    int usages;

    // +++ Repository +++
    @Nullable
    public static TagStatistics find(final UUID tagId) {
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

    public static List<TagStatistics> findAll(final PageableSearchRequestParams requestParams) {
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

    public static List<TagStatistics> findAllPopular(final PageableRequestParams requestParams) {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_MOST_USED)) {

            statement.setShort(1, requestParams.pageSize());
            statement.setInt(2, requestParams.pageSize() * (requestParams.page() - 1));

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
    // --- Repository ---

    // +++ Mapping +++
    @Nullable
    private static TagStatistics map(final ResultSet rs) throws SQLException {
        TagStatistics found = null;
        if (rs.next()) {
            found = readFromResultSet(rs);
        }
        closeResultSet(rs);

        return found;
    }

    private static List<TagStatistics> mapToList(final ResultSet rs) throws SQLException {
        final List<TagStatistics> found = new ArrayList<>();
        while (rs.next()) {
            found.add(readFromResultSet(rs));
        }
        closeResultSet(rs);

        return found;
    }

    private static TagStatistics readFromResultSet(final ResultSet rs) throws SQLException {
        return new TagStatistics()
                .tag(Tag.find(rs.getObject("tag_id", UUID.class)))
                .usages(rs.getInt("usages"));
    }
    // --- Mapping ---

    // +++ Statements +++
    private static final String STATEMENT_FIND = """
            SELECT tag_id, COUNT(information_note_id) AS usages FROM information_notes_tags
            WHERE tag_id = ?
            GROUP BY tag_id
            """;

    private static final String STATEMENT_FIND_ALL_PAGEABLE = """
            WITH search_str AS (
                SELECT CONCAT('%%', LOWER(REPLACE(CAST(? AS varchar), ' ', '')), '%%')
            )
            SELECT tags.tag_id AS tag_id, COUNT(information_note_id) AS usages FROM tags
            LEFT JOIN information_notes_tags ON tags.tag_id = information_notes_tags.tag_id
            WHERE (SELECT * FROM search_str) IS null OR LOWER(REPLACE(name, ' ', '')) LIKE (SELECT * FROM search_str)
            GROUP BY tag_id, name
            ORDER BY name
            LIMIT ? + 1 OFFSET ?
            """;

    private static final String STATEMENT_FIND_MOST_USED = """
            SELECT tag_id, COUNT(information_note_id) AS usages FROM information_notes_tags
            GROUP BY tag_id
            HAVING COUNT(information_note_id) > 0
            ORDER BY usages DESC
            LIMIT ? + 1 OFFSET ?
            """;
    // --- Statements ---

}
