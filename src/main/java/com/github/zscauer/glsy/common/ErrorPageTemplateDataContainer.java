package com.github.zscauer.glsy.common;

import io.quarkus.qute.TemplateData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Container of content for templates, those contains HTTP error code to show and should be used for error pages,
 * where is no content to show (such as internal server errors).
 *
 * @param <T> expected type of content
 */
@TemplateData
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ErrorPageTemplateDataContainer<T> extends TemplateDataContainer<T> {

    /**
     * {@linkplain jakarta.ws.rs.core.Response.Status}
     */
    int errorCode;

    public ErrorPageTemplateDataContainer() {
        super(null, null);
    }

    @Override
    @SuppressWarnings("unused")
    public boolean errorPageContainer() {
        return true;
    }

}
