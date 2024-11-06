package com.github.zscauer.glsy.i18n;

import com.github.zscauer.glsy.common.TemplateUsage;
import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@TemplateUsage
@MessageBundle("msg_global")
@SuppressWarnings("unused")
public interface AppMessagesGlobal {

    @Message
    String date_format();

    @Message
    String date_time_format();

    @Message
    String date_time_seconds_format();

    @Message
    String service_name();

    @Message
    String service_version();

    @Message
    String on_construction();

    @Message
    String go_to_main_page();

    @Message
    String yes();

    @Message
    String no();

    @Message
    String oops();

    @Message
    String or();

    @Message
    String not_implemented_text();

    @Message
    String error_internal();

    @Message
    String error_unique_violation_constraint();

}
