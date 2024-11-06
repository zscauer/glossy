package com.github.zscauer.glsy.knowledges;

import com.github.zscauer.glsy.common.TemplateUsage;
import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@TemplateUsage
@MessageBundle("msg_knowledges")
@SuppressWarnings("unused")
public interface AppMessagesKnowledges {

    @Message
    String information_note_create();

    @Message
    String information_note_upsert_new_title();

    @Message
    String information_note_upsert_edit_title();

    @Message
    String information_note_upsert_name_field_title();

    @Message
    String information_note_upsert_name_field_placeholder();

    @Message
    String information_note_upsert_description_field_title();

    @Message
    String information_note_upsert_description_field_placeholder();

    @Message
    String information_note_upsert_save_button();

    @Message
    String information_notes();

    @Message
    String information_note_name();

    @Message
    String information_note_description();

    @Message
    String information_note_actual();

    @Message
    String information_note_exclude_tag_confirmation();

    @Message
    String information_note_edit_button_hint();

    @Message
    String information_note_view_history_button_hint();

    @Message
    String information_notes_search_field_placeholder();

    @Message
    String information_notes_history_updating_not_supported();

    @Message
    String information_notes_history_event_title();

    @Message
    String information_notes_history_event_time_title();

    @Message
    String information_notes_history_author_title();

    @Message
    String information_notes_history_record_view_title(final String recordTime);

    @Message
    String information_notes_history_record_view_from_address();

    @Message
    String tags();

    @Message
    String most_used_tags();

    @Message
    String tags_search_field_placeholder();

    @Message
    String tags_add_placeholder();

    @Message
    String tag_name_empty_error();

    @Message
    String tag_not_found_error();

}
