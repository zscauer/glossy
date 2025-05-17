package com.github.zscauer.glsy.knowledges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.zscauer.glsy.common.ActiveRecord;
import com.github.zscauer.glsy.common.PageableRequestParams;
import com.github.zscauer.glsy.i18n.Language;
import com.github.zscauer.glsy.i18n.MessagesProvider;
import com.github.zscauer.glsy.tools.Constants;
import io.quarkus.qute.TemplateData;
import io.vertx.core.net.SocketAddress;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@TemplateData
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class InformationNoteHistory extends ActiveRecord {

    @Nullable
    Long id;
    UUID informationNoteId;
    LifecycleEvent event = LifecycleEvent.CREATED;
    LocalDateTime eventTime = Constants.getCurrentTimeInUTC();
    String author;
    String state;

    @Override
    public boolean isNew() {
        return id == null;
    }

    public static void addHistory(final InformationNote informationNote, final LifecycleEvent event, final SocketAddress remoteAddress) {
        if (!Constants.informationNotesHistoryTrackingEnabled()) {
            return;
        }

        if (informationNote != null) {
            try {
                checkNonNullableFields(InformationNote.class, informationNote.id());
                final String changesAuthor = remoteAddress.hostName() == null ? remoteAddress.toString() : "%s(%s)".formatted(remoteAddress, remoteAddress.hostName());

                final InformationNoteHistory history = new InformationNoteHistory()
                        .informationNoteId(informationNote.id())
                        .event(event)
                        .author(changesAuthor)
                        .state(informationNote.toString());

                history.save();
            } catch (final Exception e) {
                log.warn("Unable to add information note history record for '{}': {}", informationNote.id(), e.getMessage());
            }
        }
    }

    // +++ Repository +++
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public InformationNoteHistory save() {
        if (!isNew()) {
            throw new IllegalStateException(
                    MessagesProvider.knowledges(Language.defaultLanguage()).information_notes_history_updating_not_supported());
        }
        checkNonNullableFields(InformationNoteHistory.class, informationNoteId);

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_SAVE)) {
            statement.setObject(1, informationNoteId);
            statement.setObject(2, event.name());
            statement.setTimestamp(3, Timestamp.valueOf(eventTime));
            statement.setString(4, author);
            statement.setString(5, state);

            statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage());
        }

        return this;
    }

    public static InformationNoteHistory find(final long id) {
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND)) {
            statement.setLong(1, id);

            return map(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static List<InformationNoteHistory> findForInformationNote(final UUID informationNoteId,
                                                                      final PageableRequestParams requestParams) {
        Objects.requireNonNull(informationNoteId);

        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND_FOR_INFORMATION_NOTE)) {
            statement.setObject(1, informationNoteId);
            statement.setShort(2, requestParams.pageSize());
            statement.setInt(3, requestParams.pageSize() * (requestParams.page() - 1));

            return mapToList(statement.executeQuery());

        } catch (final SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
    // --- Repository ---

    // +++ Mapping +++
    @Nullable
    private static InformationNoteHistory map(final ResultSet rs) throws SQLException {
        InformationNoteHistory found = null;
        if (rs.next()) {
            found = readFromResultSet(rs);
        }
        closeResultSet(rs);

        return found;
    }

    private static List<InformationNoteHistory> mapToList(final ResultSet rs) throws SQLException {
        final List<InformationNoteHistory> found = new ArrayList<>();
        while (rs.next()) {
            found.add(readFromResultSet(rs));
        }
        closeResultSet(rs);

        return found;
    }

    private static InformationNoteHistory readFromResultSet(final ResultSet rs) throws SQLException {
        return new InformationNoteHistory()
                .id(rs.getLong("information_note_history_id"))
                .informationNoteId(rs.getObject("information_note_id", UUID.class))
                .event(LifecycleEvent.valueOf(rs.getString("event")))
                .eventTime(rs.getTimestamp("event_time").toLocalDateTime())
                .author(rs.getString("author"))
                .state(rs.getString("state"));
    }
    // --- Mapping ---

    // +++ Statements +++
    private static final String STATEMENT_SAVE = """
            INSERT INTO information_notes_history (information_note_id, event, event_time, author, state)
            VALUES (?, CAST(? AS lifecycle_event), ?, ?, ?)
            """;

    private static final String STATEMENT_FIND = """
            SELECT * FROM information_notes_history
            WHERE information_note_history_id = ?
            """;

    private static final String STATEMENT_FIND_FOR_INFORMATION_NOTE = """
            SELECT * FROM information_notes_history
            WHERE information_note_id = ?
            ORDER BY event_time DESC
            LIMIT ? + 1 OFFSET ?
            """;
    // --- Statements ---
}
