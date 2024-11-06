package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.TemplateUsage;
import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@TemplateUsage
@MessageBundle("msg_security")
@SuppressWarnings("unused")
public interface AppMessagesSecurity {

    @Message
    String unable_to_grant_privileges();

    @Message
    String secret_not_provided_error();

    @Message
    String secret_not_matched_error();

    @Message
    String access_mode_title();

    @Message
    String request_change_access_mode_button();

    @Message
    String request_change_access_mode_title();

    @Message
    String request_change_access_mode_requested_mode_selector_title();

    @Message
    String request_change_access_mode_secret_field_title();

    @Message
    String request_change_access_mode_secret_field_placeholder();

    @Message
    String request_change_access_mode_enable_selected_mode_button();

    @Message
    String request_change_access_mode_current_mode_title();

    @Message
    String request_change_access_mode_disable_current_mode_button();

    @Message
    String request_change_access_mode_disable_current_mode_confirm();

    @Message
    String no_privilege_to_create_information_notes();

    @Message
    String no_privilege_to_update_information_notes();

    @Message
    String no_privilege_to_create_tags();

    @Message
    String no_privilege_to_view_information_note_history();

}
